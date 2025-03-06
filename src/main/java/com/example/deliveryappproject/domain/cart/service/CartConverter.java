package com.example.deliveryappproject.domain.cart.service;

import com.example.deliveryappproject.domain.cart.model.CartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartConverter {

    public List<Long> toCarItemIds(List<CartItem> items) {
        return items.stream().map(item -> item.getItemId()).toList();
    }
}
