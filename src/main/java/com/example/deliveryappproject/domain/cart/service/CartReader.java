package com.example.deliveryappproject.domain.cart.service;

import com.example.deliveryappproject.common.exception.BadRequestException;
import com.example.deliveryappproject.domain.cart.model.CartItem;
import com.example.deliveryappproject.domain.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartReader {
    private final CartRepository cartRepository;

    public Optional<Long> readStoreId(Long userId) {
        Object storeId = cartRepository.findStoreId(userId);
        if (storeId == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong((String) storeId));
        } catch (NumberFormatException e) {
            log.error("NumberFormatException: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    public List<CartItem> readItems(Long userId) {
        return cartRepository.findItems(userId);
    }

}
