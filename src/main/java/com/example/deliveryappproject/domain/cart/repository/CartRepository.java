package com.example.deliveryappproject.domain.cart.repository;

import com.example.deliveryappproject.domain.cart.dto.CartItemRequest;
import com.example.deliveryappproject.domain.cart.dto.CartItemsRequest;
import com.example.deliveryappproject.domain.cart.model.CartItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.example.deliveryappproject.domain.cart.util.RedisKeyUtil.ITEM_PREFIX;
import static com.example.deliveryappproject.domain.cart.util.RedisKeyUtil.STORE_ID_KEY;
import static com.example.deliveryappproject.domain.cart.util.RedisKeyUtil.extractItemId;
import static com.example.deliveryappproject.domain.cart.util.RedisKeyUtil.getCartKey;
import static com.example.deliveryappproject.domain.cart.util.RedisKeyUtil.getItemKey;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CartRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final long durationTime = 5L;


    public void saveItems(Long userId, CartItemsRequest cartItemsRequest) {
        String cartKey = getCartKey(userId);
        long storeId = cartItemsRequest.getStoreId();
        redisTemplate.opsForHash().put(cartKey, STORE_ID_KEY, String.valueOf(storeId));
        for (CartItemRequest item : cartItemsRequest.getItems()) {
            String itemKey = getItemKey(item.getItemId());
            redisTemplate.opsForHash().increment(cartKey, itemKey, item.getQuantity());
        }
        refreshCartExpiration(cartKey);
    }

    public List<CartItem> findItems(Long userId) {
        String cartKey = getCartKey(userId);
        Map<String, String> entries = redisTemplate.<String, String>opsForHash().entries(cartKey);

        if (CollectionUtils.isEmpty(entries)) {
            return List.of();
        }
        return entries.entrySet().stream()
                .filter(entry-> entry.getKey().startsWith(ITEM_PREFIX))
                .map(this::convertToCartItem)
                .toList();
    }

    public Long findStoreId(long userId) {
        String cartKey = getCartKey(userId);
        Object storeIdObj = redisTemplate.opsForHash().get(cartKey, STORE_ID_KEY);
        if (storeIdObj == null) {
            return null;
        }
        return Long.parseLong((String) storeIdObj);
    }



    public void clear(Long userId) {
        String cartKey = getCartKey(userId);
        redisTemplate.delete(cartKey);
    }

    public void deleteItem(Long userId, Long itemId) {
        String cartKey = getCartKey(userId);
        String itemKey = getItemKey(itemId);
        redisTemplate.opsForHash().delete(cartKey, itemKey);
        refreshCartExpiration(cartKey);
    }

    public void decreaseItemQuantity(Long userId, Long itemId, int quantity) {
        String cartKey = getCartKey(userId);
        String itemKey = getItemKey(itemId);
        Long newQuantity = redisTemplate.opsForHash().increment(cartKey, itemKey, -quantity);

        if (newQuantity <= 0) {
            redisTemplate.opsForHash().delete(cartKey, itemKey);
        }
        refreshCartExpiration(cartKey);
    }

    private void refreshCartExpiration(String cartKey) {
        redisTemplate.expire(cartKey, Duration.ofMinutes(durationTime));
    }

    private CartItem convertToCartItem(Map.Entry<String, String> entry) {
            Long itemId = extractItemId(entry.getKey());
            int quantity = Integer.parseInt(entry.getValue());
            return new CartItem(itemId, quantity);
    }

}
