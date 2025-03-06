package com.example.deliveryappproject.domain.cart.service;

import com.example.deliveryappproject.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartFinder {

    private final CartReader cartReader;


    public Long findStoreId(Long userId) {
        return cartReader.readStoreId(userId).orElseThrow(() -> {
            throw new NotFoundException("cart is empty");
        });

    }

}
