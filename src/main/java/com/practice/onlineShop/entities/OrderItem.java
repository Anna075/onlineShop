package com.practice.onlineShop.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Setter
@Getter
public class OrderItem {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private long id;

    @OneToOne // one orderItem
    private Product product;
    private int quantity;

}
