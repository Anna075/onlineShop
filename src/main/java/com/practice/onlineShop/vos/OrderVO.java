package com.practice.onlineShop.vos;

import lombok.Data;

import java.util.Map;

@Data
public class OrderVO {
    private Integer userId;
    private Map<Integer, Integer> productIdsToQuantity; // Product 1 - Quantity 3

}
