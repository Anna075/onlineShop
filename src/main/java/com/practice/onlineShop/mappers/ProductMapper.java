package com.practice.onlineShop.mappers;

import com.practice.onlineShop.entities.Product;
import com.practice.onlineShop.vos.ProductVo;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper{

        public Product toEntity(ProductVo productVo){

            if(productVo == null){
                return null;
            }
            Product product = new Product();
            product.setId(productVo.getId());
            product.setPrice(productVo.getPrice());
            product.setCode(productVo.getCode());
            product.setDescription(productVo.getDescription());
            product.setStock(productVo.getStock());
            product.setValid(productVo.isValid());
            product.setCurrency(productVo.getCurrency());
            return product;
        }

        public ProductVo toVO(Product product){
            if(product == null){
                return null;
            }
            ProductVo productVo = new ProductVo();
            productVo.setId(product.getId());
            productVo.setPrice(product.getPrice());
            productVo.setCode(product.getCode());
            productVo.setDescription(product.getDescription());
            productVo.setStock(product.getStock());
            productVo.setValid(product.isValid());
            productVo.setCurrency(product.getCurrency());
            return productVo;
        }
}
