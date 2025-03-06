package com.example.deliveryappproject.domain.order.service;

import com.example.deliveryappproject.common.exception.NotFoundException;
import com.example.deliveryappproject.domain.cart.model.CartItem;
import com.example.deliveryappproject.domain.menu.entity.Menu;
import com.example.deliveryappproject.domain.menu.service.MenuConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PriceCalculator {

    private final MenuConverter menuConverter;


    public BigDecimal calculateCartTotalPrice(List<Menu> menus, List<CartItem> cartItems) {
        Map<Long, Menu> menuMap = menuConverter.convertToMenuMap(menus);
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            Long itemId = cartItem.getItemId();
            int quantity = cartItem.getQuantity();

            Menu menu = menuMap.get(itemId);

            BigDecimal menuTotalPrice = menu.getPrice().multiply(BigDecimal.valueOf(quantity));
            totalPrice = totalPrice.add(menuTotalPrice);
        }
        return totalPrice;
    }

}
