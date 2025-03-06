package com.example.deliveryappproject.domain.menu.service;

import com.example.deliveryappproject.domain.menu.entity.Menu;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MenuConverter {
    public Map<Long, Menu> convertToMenuMap(List<Menu> menus) {
        return menus.stream().collect(Collectors.toMap(Menu::getId, menu -> menu));
    }

}
