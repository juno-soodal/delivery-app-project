package com.example.deliveryappproject.domain.order.service;

import com.example.deliveryappproject.domain.cart.model.CartItem;
import com.example.deliveryappproject.domain.menu.entity.Menu;
import com.example.deliveryappproject.domain.menu.service.MenuConverter;
import com.example.deliveryappproject.domain.order.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderItemProcessor {

    private final OrderItemConverter orderItemConverter;
    private final MenuConverter menuConverter;

    public List<OrderItem> convertCartItemsToOrderItems(List<CartItem> cartItems, List<Menu> menus) {
        Map<Long, Menu> menuMap = menuConverter.convertToMenuMap(menus);
        return cartItems.stream()
                .map(cartItem -> orderItemConverter.convertToOrderItem(cartItem, menuMap))
                .toList();
    }

}
