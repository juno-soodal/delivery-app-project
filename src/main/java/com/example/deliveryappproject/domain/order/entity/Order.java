package com.example.deliveryappproject.domain.order.entity;

import com.example.deliveryappproject.domain.delivery.entity.Delivery;
import com.example.deliveryappproject.domain.store.entity.Store;
import com.example.deliveryappproject.domain.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private int usedPoints;

    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();


    private Order(User user, Store store, int usePoints) {
        this.user = user;
        this.store = store;
        this.usedPoints = usePoints;
        this.orderStatus = OrderStatus.PENDING;
    }

    public static Order createOrder(User user, Store store, int usePoints, List<OrderItem> orderItems) {
        Order order = new Order(user, store, usePoints);
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        return order;
    }


    public void addOrderItem(OrderItem orderItem) {
        orderItem.setOrder(this);
        this.orderItems.add(orderItem);
    }

    public void acceptOrder() {
        this.orderStatus = OrderStatus.ACCEPT;
    }

    public BigDecimal getTotalPrice() {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderItem orderItem : orderItems) {
            BigDecimal orderPrice = orderItem.getOrderPrice();
            totalPrice = totalPrice.add(orderPrice);
        }
        return totalPrice;
    }

    public void rejectOrder() {
        this.orderStatus = OrderStatus.REJECT;
    }

    public void completeOrder() {
        this.orderStatus = OrderStatus.COMP;
    }

    public void cancelOrder() {
        this.orderStatus = OrderStatus.CANCEL;
    }

    public boolean isPending() {
        return this.orderStatus == OrderStatus.PENDING;
    }

    public boolean isOwner(Long userId) {
        return this.user != null && Objects.equals(this.user.getId(),userId);
    }

}
