package com.practice.onlineShop.services;

import com.practice.onlineShop.entities.Orders;
import com.practice.onlineShop.entities.Product;
import com.practice.onlineShop.exceptions.*;
import com.practice.onlineShop.mappers.OrderMapper;
import com.practice.onlineShop.repositories.OrderRepository;
import com.practice.onlineShop.vos.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final StockService stockService;

    public void addOrder(OrderVO orderVO) throws InvalidCustomerIdException, InvalidProductsException, InvalidProductIdException, NotEnoughStockException {
        validateStock(orderVO);

        Orders order = orderMapper.toEntity(orderVO);
        order.getOrderItems().forEach(orderItem -> {
            int oldProductStock = orderItem.getProduct().getStock();
            int productId = (int) orderItem.getProduct().getId();
            int sellStock = orderVO.getProductIdsToQuantity().get(productId);
            orderItem.getProduct().setStock(oldProductStock - sellStock);
        });
        orderRepository.save(order);
    }

    @Transactional
    public void deliver(Integer orderId, Long customerId) throws InvalidOrderIdException, OrderCanceledException {
        System.out.println("Customer-ul cu id-ul: " + customerId + " este in service!");
        throwExceptionIfOrderIDisAbsent(orderId);

        Orders order = getOrderOrThrowException(orderId);
        if(order.isCanceled()){
            throw new OrderCanceledException();
        }


        order.setDelivered(true);
    }

    @Transactional
    public void cancelOrder(Integer orderId, Long customerId) throws InvalidOrderIdException, OrderAlreadyDeliveredException {
        System.out.println("Customer-ul cu id-ul: "  + customerId + " este in service pentru a anula comanda " + orderId);

        throwExceptionIfOrderIDisAbsent(orderId);
        Orders order = getOrderOrThrowException(orderId);
        if(order.isDelivered()){
            throw new OrderAlreadyDeliveredException();
        }
        order.setCanceled(true);
    }

    @Transactional
    public void returnOrder(Integer orderId, Long customerId) throws InvalidOrderIdException, OrderNotDeliveredYetException, OrderCanceledException {
        System.out.println("Customer-ul cu id-ul: "  + customerId + " este in service pentru a returna comanda " + orderId);
        throwExceptionIfOrderIDisAbsent(orderId);
        Orders order = getOrderOrThrowException(orderId);

        if(!order.isDelivered()){
            throw new OrderNotDeliveredYetException();
        }
        if(order.isCanceled()){
            throw new OrderCanceledException();
        }
        order.setReturned(true);

        order.getOrderItems().forEach(orderItem -> {
            Product product = orderItem.getProduct();
            int oldStock = product.getStock();
            product.setStock(oldStock + orderItem.getQuantity());
        });
    }

    private void throwExceptionIfOrderIDisAbsent(Integer orderId) throws InvalidOrderIdException {
        if(orderId == null){
            throw new InvalidOrderIdException();
        }
    }

    private Orders getOrderOrThrowException(Integer orderId) throws InvalidOrderIdException{
        Optional<Orders> ordersOptional = orderRepository.findById(orderId.longValue());
        if(!ordersOptional.isPresent()){
            throw new InvalidOrderIdException();
        }
        return ordersOptional.get();
    }

    private void validateStock(OrderVO orderVO) throws NotEnoughStockException {
        Map<Integer, Integer> productIdsToQuantityMap = orderVO.getProductIdsToQuantity();
        Set<Integer> productsIds = productIdsToQuantityMap.keySet();
        for(Integer productId : productsIds){
            Integer quantity = productIdsToQuantityMap.get(productId);
            boolean havingEnoughStock = stockService.isHavingEnoughStock(productId, quantity);
            if(!havingEnoughStock){
                throw new NotEnoughStockException();
            }
        }
    }
}
