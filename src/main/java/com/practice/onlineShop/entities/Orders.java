package com.practice.onlineShop.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;


import java.util.Collection;
import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Setter
@Getter
public class Orders {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private long id;

    @OneToMany(cascade = CascadeType.ALL) // one order to many orderItems
    @JoinColumn(name = "order_id")
    private List<OrderItem> orderItems;
    @OneToOne // un order poate avea doar un user
    @JoinColumn(name = "user_id")
    private User user;
    private boolean isDelivered;
    private boolean isReturned;
    private boolean isCanceled;
}
