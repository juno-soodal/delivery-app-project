package com.example.deliveryappproject.domain.menu.service;

import com.example.deliveryappproject.common.exception.NotFoundException;
import com.example.deliveryappproject.domain.menu.entity.Menu;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MenuValidator {
    public void validateCartItemsExist(List<Long> cartItemIds, List<Menu> menus) {

        Set<Long> menuIds = menus.stream().map(menu -> menu.getId()).collect(Collectors.toSet());

        if (!menuIds.containsAll(cartItemIds)) {
            throw new NotFoundException("Some items in the cart are not available in the menu.");
        }
    }
}
