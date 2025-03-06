package com.example.deliveryappproject.domain.store.service;

import com.example.deliveryappproject.common.exception.NotFoundException;
import com.example.deliveryappproject.domain.store.entity.Store;
import com.example.deliveryappproject.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreReader {

    private final StoreRepository storeRepository;

    public Store read(Long storeId) {
        return storeRepository.findById(storeId).orElseThrow(() -> new NotFoundException("Not Found Store"));
    }
}
