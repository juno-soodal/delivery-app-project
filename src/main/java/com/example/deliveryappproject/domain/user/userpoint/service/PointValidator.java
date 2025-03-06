package com.example.deliveryappproject.domain.user.userpoint.service;

import com.example.deliveryappproject.common.exception.ForbiddenException;
import com.example.deliveryappproject.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.example.deliveryappproject.domain.order.exception.ErrorMessages.POINT_NOT_ENOUGH;

@Service
@RequiredArgsConstructor
public class PointValidator {

    public void validateUsePoints(User user, int usePoints) {
        if (usePoints > 0 && usePoints > user.getPoint()) {
            throw new ForbiddenException(POINT_NOT_ENOUGH);
        }
    }
}
