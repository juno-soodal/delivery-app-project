package com.example.deliveryappproject.domain.menu.service;

import com.example.deliveryappproject.domain.menu.entity.Menu;
import com.example.deliveryappproject.domain.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuReader {

    private final MenuRepository menuRepository;

    public List<Menu> readMenus(List<Long> cartItemIds) {
        return menuRepository.findAllById(cartItemIds);
    }

}
