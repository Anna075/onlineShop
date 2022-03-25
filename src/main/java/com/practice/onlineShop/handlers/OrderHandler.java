package com.practice.onlineShop.handlers;

import com.practice.onlineShop.exceptions.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.ResponseEntity.status;

@ControllerAdvice
public class OrderHandler {

    @ExceptionHandler(InvalidProductsException.class)
    public ResponseEntity<String> handleInvalidProductsException(){
        return status(BAD_REQUEST).body("Comanda dvs. nu contine niciun produs!");
    }

    @ExceptionHandler(InvalidCustomerIdException.class)
    public ResponseEntity<String> handleInvalidCustomerIdException(){
        return status(BAD_REQUEST).body("Comanda dvs. nu este asignata unui user valid!");
    }

    @ExceptionHandler(InvalidProductIdException.class)
    public ResponseEntity<String> handleInvalidProductIdException(){
        return status(BAD_REQUEST).body("Id-ul unui produs nu este valid in comanda curenta!");
    }

    @ExceptionHandler(NotEnoughStockException.class)
    public ResponseEntity<String> handleNotEnoughStockException(){
        return status(BAD_REQUEST).body("Un produs nu a avut stocul necesar!");
    }

    @ExceptionHandler(InvalidOrderIdException.class)
    public ResponseEntity<String> handleInvalidOrderIdException(){
        return status(BAD_REQUEST).body("Id-ul comenzii nu este valid!");
    }

    @ExceptionHandler(OrderAlreadyDeliveredException.class)
    public ResponseEntity<String> handleOrderAlreadyDeliveredException(){
        return status(BAD_REQUEST).body("Comanda deja a fost livrata!");
    }

    @ExceptionHandler(OrderCanceledException.class)
    public ResponseEntity<String> handleOrderCanceledException(){
        return status(BAD_REQUEST).body("Comanda a fost anulata!");
    }

    @ExceptionHandler(OrderNotDeliveredYetException.class)
    public ResponseEntity<String> handleOrderNotDeliveredYetException(){
        return status(BAD_REQUEST).body("Comanda nu poate fi returnata deoarece nu a fost livrata!");
    }
}
