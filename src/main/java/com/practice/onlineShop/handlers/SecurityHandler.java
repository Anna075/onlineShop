package com.practice.onlineShop.handlers;

import com.practice.onlineShop.exceptions.InvalidCustomerIdException;
import com.practice.onlineShop.exceptions.InvalidOperationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.ResponseEntity.status;

@ControllerAdvice
public class SecurityHandler{

    @ExceptionHandler(InvalidCustomerIdException.class)
    public ResponseEntity<String> handleInvalidCustomerIdException(){
        return status(BAD_REQUEST).body("Id-ul trimis este invalid!");
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<String> handleInvalidOperationException(){
        return status(BAD_REQUEST).body("Utilizatorul nu are permisiunea de a executa aceasta operatiune!");
    }
}
