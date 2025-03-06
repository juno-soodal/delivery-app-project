package com.example.deliveryappproject.domain.store.service;

import com.example.deliveryappproject.common.exception.ForbiddenException;
import com.example.deliveryappproject.domain.store.entity.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

import static com.example.deliveryappproject.domain.order.exception.ErrorMessages.ORDER_NOT_AVAILABLE;

@Service
@RequiredArgsConstructor
public class StoreFinder {

    private final StoreReader storeReader;

    public Store findOrderableStore(Long storeId, LocalTime orderTime) {
        Store store = storeReader.read(storeId);
        validateOrderAvailability(orderTime, store);
        return store;
    }

    private void validateOrderAvailability(LocalTime orderTime, Store store) {
        if (!store.isOrderAvailable(orderTime)) {
            throw new ForbiddenException(ORDER_NOT_AVAILABLE);
        }
    }
}
