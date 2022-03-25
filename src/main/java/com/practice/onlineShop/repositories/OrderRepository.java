package com.practice.onlineShop.repositories;

import com.practice.onlineShop.entities.Orders;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<Orders, Long> {
}
