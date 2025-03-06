package com.example.deliveryappproject.domain.cart.service;

import com.example.deliveryappproject.domain.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartWriter {

    private final CartRepository cartRepository;

    public void clear(Long userId) {

        cartRepository.clear(userId);
    }
}
