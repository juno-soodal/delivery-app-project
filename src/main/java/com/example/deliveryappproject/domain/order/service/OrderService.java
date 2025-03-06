package com.example.deliveryappproject.domain.order.service;

import com.example.deliveryappproject.common.exception.ForbiddenException;
import com.example.deliveryappproject.common.exception.NotFoundException;
import com.example.deliveryappproject.domain.cart.model.CartItem;
import com.example.deliveryappproject.domain.cart.service.CartFinder;
import com.example.deliveryappproject.domain.cart.service.CartConverter;
import com.example.deliveryappproject.domain.cart.service.CartReader;
import com.example.deliveryappproject.domain.cart.service.CartWriter;
import com.example.deliveryappproject.domain.delivery.entity.Delivery;
import com.example.deliveryappproject.domain.delivery.repository.DeliveryRepository;
import com.example.deliveryappproject.domain.menu.entity.Menu;
import com.example.deliveryappproject.domain.menu.service.MenuReader;
import com.example.deliveryappproject.domain.menu.service.MenuValidator;
import com.example.deliveryappproject.domain.order.dto.OrderDetailResponse;
import com.example.deliveryappproject.domain.order.dto.OrderRequest;
import com.example.deliveryappproject.domain.order.entity.Order;
import com.example.deliveryappproject.domain.order.entity.OrderItem;
import com.example.deliveryappproject.domain.order.repository.OrderRepository;
import com.example.deliveryappproject.domain.order.service.dto.OrderResponse;
import com.example.deliveryappproject.domain.policy.PointPolicy;
import com.example.deliveryappproject.domain.store.entity.Store;
import com.example.deliveryappproject.domain.store.service.StoreFinder;
import com.example.deliveryappproject.domain.user.entity.User;
import com.example.deliveryappproject.domain.user.service.UserReader;
import com.example.deliveryappproject.domain.user.userpoint.PointHistoryRepository;
import com.example.deliveryappproject.domain.user.userpoint.entity.PointHistory;
import com.example.deliveryappproject.domain.user.userpoint.entity.PointType;
import com.example.deliveryappproject.domain.user.userpoint.service.PointValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import static com.example.deliveryappproject.domain.order.exception.ErrorMessages.MIN_ORDER_AMOUNT_REQUIRED;
import static com.example.deliveryappproject.domain.order.exception.ErrorMessages.ORDER_NOT_FOUND;
import static com.example.deliveryappproject.domain.order.exception.ErrorMessages.ORDER_NOT_OWNER;
import static com.example.deliveryappproject.domain.order.exception.ErrorMessages.ORDER_STATUS_NOT_PENDING;
import static com.example.deliveryappproject.domain.order.exception.ErrorMessages.POINT_NOT_ENOUGH;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final PointPolicy pointPolicy;

    private final CartFinder cartFinder;
    private final StoreFinder storeFinder;
    private final CartReader cartReader;
    private final CartConverter cartConverter;
    private final MenuReader menuReader;
    private final PriceCalculator priceCalculator;
    private final MenuValidator menuValidator;
    private final UserReader userReader;
    private final PointValidator pointValidator;
    private final CartWriter cartWriter;
    private final OrderItemProcessor orderItemProcessor;


    @Transactional
    public OrderResponse order(Long userId, OrderRequest orderRequest) {

        //장바구니 조회
        Long storeId = cartFinder.findStoreId(userId);

        //가게 조회 및 검증
        LocalTime orderTime = LocalTime.now();
        Store store = storeFinder.findOrderableStore(storeId, orderTime);

        List<CartItem> cartItems = cartReader.readItems(userId);

        List<Long> cartItemIds = cartConverter.toCarItemIds(cartItems);

        List<Menu> menus = menuReader.readMenus(cartItemIds);

        menuValidator.validateCartItemsExist(cartItemIds, menus);

        BigDecimal totalPrice = priceCalculator.calculateCartTotalPrice(menus, cartItems);

        validateMinOrderAmount(store, totalPrice);

        User user = userReader.read(userId);

        pointValidator.validateUsePoints(user, orderRequest.getUsePoints());

        List<OrderItem> orderItems = orderItemProcessor.convertCartItemsToOrderItems(cartItems, menus);

        Order order = Order.createOrder(user, store, orderRequest.getUsePoints(), orderItems);
        //주문 저장
        orderRepository.save(order);

        //장바구니 초기화
        cartWriter.clear(userId);

        //끝
        return OrderResponse.of(order.getId(),storeId, order.getOrderStatus());

    }

    private void validateUsePoints(User user, int usePoints) {
        if (usePoints > 0 && usePoints > user.getPoint()) {
            throw new ForbiddenException(POINT_NOT_ENOUGH);
        }
    }


    @Transactional
    public OrderResponse acceptOrder(Long userId, Long orderId) {
        Order order = findByIdOrElseThrow(orderId);

        validateStoreOwner(order, userId);
        validateOrderStatusPending(order);

        User user = userReader.read(userId);

        validateUsePoints(user, order.getUsedPoints());

        handlePoints(order, user);

        order.acceptOrder();

        Delivery delivery = new Delivery(order);
        deliveryRepository.save(delivery);

        return OrderResponse.of(order.getId(),order.getStore().getId(), order.getOrderStatus());

    }

    @Transactional
    public OrderResponse rejectOrder(Long userId, Long orderId) {
        Order order = findByIdOrElseThrow(orderId);

        validateStoreOwner(order, userId);
        validateOrderStatusPending(order);

        order.rejectOrder();

        return OrderResponse.of(order.getId(),order.getStore().getId(), order.getOrderStatus());
    }


    @Transactional
    public OrderResponse cancelOrder(Long userId, Long orderId) {
        Order order = findByIdOrElseThrow(orderId);

        validateOrderStatusPending(order);
        validateOrderOwner(order, userId);


        order.cancelOrder();

        return OrderResponse.of(order.getId(),order.getStore().getId(), order.getOrderStatus());

    }


    public OrderDetailResponse getOrder(Long orderId) {
        Order order = orderRepository.findByIdWithStoreWithOrderItems(orderId).orElseThrow(() -> new NotFoundException(ORDER_NOT_FOUND));

        return OrderDetailResponse.from(order);
    }

    private void validateMinOrderAmount(Store store, BigDecimal totalPrice) {
        if (totalPrice.compareTo(store.getMinOrderPrice()) < 0) {
            throw new ForbiddenException(MIN_ORDER_AMOUNT_REQUIRED);
        }
    }

    private void handlePoints(Order order, User user) {
        if (order.getUsedPoints() > 0) {
            usePoints(order, user);
        }else{
            earnPoints(order, user);
        }
    }

    private void earnPoints(Order order, User user) {
        int calculateEarnedPoints = pointPolicy.calculateEarnedPoints(order.getTotalPrice());
        user.addPoints(calculateEarnedPoints);
        savePointHistory(user, PointType.EARN, calculateEarnedPoints, order);
    }


    private void usePoints(Order order, User user) {
        user.usePoints(order.getUsedPoints());
        savePointHistory(user, PointType.USE, order.getUsedPoints(), order);
    }

    private void savePointHistory(User user, PointType earn, int calculateEarnedPoints, Order order) {
        pointHistoryRepository.save(new PointHistory(user, earn, calculateEarnedPoints, order.getId()));
    }

    private static void validateOrderStatusPending(Order order) {
        if (!order.isPending()) {
            throw new ForbiddenException(ORDER_STATUS_NOT_PENDING);
        }
    }

    private Order findByIdOrElseThrow(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException(ORDER_NOT_FOUND));
    }

    private void validateStoreOwner(Order order, Long userId) {

        Store store = order.getStore();
        if (order.getStore() == null || !store.isOwner(userId)) {
            throw new ForbiddenException("해당 주문에 대한 권한이 없습니다.");
        }
    }

    private void validateOrderOwner(Order order, Long userId) {
        if (!order.isOwner(userId)) {
            throw new ForbiddenException(ORDER_NOT_OWNER);
        }
    }
}
