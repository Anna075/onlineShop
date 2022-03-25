package com.practice.onlineShop.services;

import com.practice.onlineShop.entities.Product;
import com.practice.onlineShop.exceptions.InvalidProductCodeException;
import com.practice.onlineShop.mappers.ProductMapper;
import com.practice.onlineShop.repositories.ProductRepository;
import com.practice.onlineShop.vos.ProductVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final ProductRepository productRepository;

    public void addProduct(ProductVo productVO, Long customerId){
        System.out.println("Customer with id " + customerId + " is in service");
        Product product = productMapper.toEntity(productVO);
        productRepository.save(product);
    }

    public ProductVo getProduct(String productCode) throws InvalidProductCodeException {
        Product product = getProductEntity(productCode);

        return productMapper.toVO(product);
    }

    public List<ProductVo>  getProducts(){
        List<ProductVo> products = new ArrayList<>();
        Iterable<Product> productsFromDBIterable = productRepository.findAll();
        Iterator<Product> iterator = productsFromDBIterable.iterator();
        while(iterator.hasNext()) {
            Product product = iterator.next();
            ProductVo productVo = productMapper.toVO(product);
            products.add(productVo);
        }
        return products;
    }

    public void updateProduct(ProductVo productVo, Long customerId) throws InvalidProductCodeException {
        System.out.println("Customer with id " + customerId + " is in service for update!");
        verifyProductCode(productVo.getCode());
        Product product = getProductEntity(productVo.getCode());
        product.setValid(productVo.isValid());
        product.setPrice(productVo.getPrice());
        product.setDescription(productVo.getDescription());
        product.setCurrency(productVo.getCurrency());
        product.setStock(productVo.getStock());

        productRepository.save(product);
    }

    public void deleteProduct(String productCode, Long customerId) throws InvalidProductCodeException {
        System.out.println("User with id: " + customerId + " is deleting " + productCode);
        verifyProductCode(productCode);

        Product product = getProductEntity(productCode);
        productRepository.delete(product);
    }

    @Transactional
    public void addStock(String productCode, Integer quantity, Long customerId ) throws InvalidProductCodeException {
        System.out.println("User with id: " + customerId + " is adding stock for " + productCode + ", number of items: " + quantity);
        verifyProductCode(productCode);
        Product product = getProductEntity(productCode);

        int oldStock = product.getStock();
        product.setStock(oldStock + quantity);

    }

    private void verifyProductCode(String productCode) throws InvalidProductCodeException {
        if(productCode == null){
            throw new InvalidProductCodeException();
        }
    }

    private Product getProductEntity(String productCode) throws InvalidProductCodeException {
        Optional<Product> productOptional = productRepository.findByCode(productCode);

        if(!productOptional.isPresent()){
            throw new InvalidProductCodeException();
        }
        return productOptional.get();
    }
}
