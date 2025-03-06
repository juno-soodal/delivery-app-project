package com.example.deliveryappproject.domain.order.service;

import com.example.deliveryappproject.common.exception.BadRequestException;
import com.example.deliveryappproject.domain.cart.model.CartItem;
import com.example.deliveryappproject.domain.menu.entity.Menu;
import com.example.deliveryappproject.domain.order.entity.OrderItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class OrderItemConverter {
    public OrderItem convertToOrderItem(CartItem cartItem, Map<Long, Menu> menuMap) {

            Long itemId = cartItem.getItemId();
            int quantity = cartItem.getQuantity();
            Menu menu = menuMap.get(itemId);

            BigDecimal menuTotalPrice = menu.getPrice().multiply(BigDecimal.valueOf(quantity));

            return OrderItem.createOrderItem(menu, menuTotalPrice, quantity);

    }

}
