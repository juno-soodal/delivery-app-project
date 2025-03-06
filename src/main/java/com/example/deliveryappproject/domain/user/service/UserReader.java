package com.example.deliveryappproject.domain.user.service;

import com.example.deliveryappproject.common.exception.NotFoundException;
import com.example.deliveryappproject.domain.user.entity.User;
import com.example.deliveryappproject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserReader {

    private final UserRepository userRepository;


    public User read(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Not Found UserId"));
    }

}
