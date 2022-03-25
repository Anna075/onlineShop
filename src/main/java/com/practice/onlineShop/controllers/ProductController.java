package com.practice.onlineShop.controllers;

import com.practice.onlineShop.exceptions.InvalidProductCodeException;
import com.practice.onlineShop.services.ProductService;
import com.practice.onlineShop.vos.ProductVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping("/{customerId}")
    public void addProduct(@RequestBody ProductVo productVo, @PathVariable Long customerId){
        productService.addProduct(productVo, customerId);

    }

    @GetMapping("/{productCode}")
    public ProductVo getProduct(@PathVariable String productCode) throws InvalidProductCodeException {
        return productService.getProduct(productCode);
    }

    @GetMapping
    public ProductVo[] getProducts(){
        return productService.getProducts().toArray(new ProductVo[]{});
    }

    @PutMapping ("/{customerId}")
    public void updateProduct(@RequestBody ProductVo productVo, @PathVariable Long customerId) throws InvalidProductCodeException {
        productService.updateProduct(productVo, customerId);
    }

    @DeleteMapping("/{productCode}/{customerId}")
    public void deleteProduct(@PathVariable String productCode, @PathVariable Long customerId) throws InvalidProductCodeException {
        productService.deleteProduct(productCode, customerId);
    }

    @PatchMapping("/{productCode}/{quantity}/{customerId}")
    public void addStock(@PathVariable String productCode, @PathVariable Integer quantity, @PathVariable Long customerId) throws InvalidProductCodeException {
        productService.addStock(productCode, quantity, customerId);
    }

}
