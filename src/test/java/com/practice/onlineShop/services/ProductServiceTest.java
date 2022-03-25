package com.practice.onlineShop.services;

import com.practice.onlineShop.entities.Product;
import com.practice.onlineShop.enums.Currencies;
import com.practice.onlineShop.exceptions.InvalidProductCodeException;
import com.practice.onlineShop.mappers.ProductMapper;
import com.practice.onlineShop.repositories.ProductRepository;
import com.practice.onlineShop.vos.ProductVo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ProductServiceTest {

    @TestConfiguration
    static class ProductServiceTestContextConfiguration {

        @MockBean
        private ProductMapper productMapper;

        @MockBean
        private ProductRepository productRepository;

        @Bean
        public ProductService productService() {
            return new ProductService(productMapper, productRepository);
        }
    }

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void addProduct() {
        Product product = new Product();
        product.setCurrency(Currencies.EUR);
        product.setPrice(11);
        product.setValid(true);
        product.setStock(1);
        product.setCode("aProductCode");

        when(productMapper.toEntity(any())).thenReturn(product);

        ProductVo productVo = new ProductVo();
        productVo.setValid(true);
        productVo.setDescription("A description");
        productVo.setStock(1);
        productVo.setPrice(11);
        productVo.setCurrency(Currencies.EUR);
        productVo.setId(1);
        productVo.setCode("aProductCode");

        Long customerId = 99L;
        productService.addProduct(productVo, customerId);

        verify(productMapper).toEntity(productVo);
        verify(productRepository).save(product);
    }

    @Test
    public void getProductWhenProductIsNotInDBShouldThrowAnException() {
        try {
            productService.getProduct("asd");
        } catch (InvalidProductCodeException e) {
            assert true;
            return;
        }
        assert false;
    }

    @Test
    public void getProductWhenIsInDBShouldReturnIt() throws InvalidProductCodeException {
        Product product = new Product();
        product.setCode("aCode");
        when(productRepository.findByCode(any())).thenReturn(Optional.of(product));
        ProductVo productVO = new ProductVo();
        productVO.setCode("aCode");
        when(productMapper.toVO(any())).thenReturn(productVO);
        ProductVo returnedProduct = productService.getProduct("aCode");

        assertThat(returnedProduct.getCode()).isEqualTo("aCode");

        verify(productRepository).findByCode("aCode");
        verify(productMapper).toVO(product);
    }

    @Test
    public void getProducts(){
        ArrayList<Product> products = new ArrayList<>();
        Product productOne = new Product();
        productOne.setCode("aCode");
        products.add(productOne);
        Product productTwo = new Product();
        productTwo.setCode("aCode2");
        products.add(productTwo);

        ProductVo productVO1 = new ProductVo();
        productVO1.setCode("aCode");
        ProductVo productVO2 = new ProductVo();
        productVO2.setCode("aCode2");

        when(productRepository.findAll()).thenReturn(products);
        when(productMapper.toVO(productOne)).thenReturn(productVO1);
        when(productMapper.toVO(productTwo)).thenReturn(productVO2);

        List<ProductVo> productsList = productService.getProducts();

        assertThat(productsList).hasSize(2);
        assertThat(productsList).containsOnly(productVO1, productVO2);

        verify(productRepository).findAll();
        verify(productMapper).toVO(productOne);
        verify(productMapper).toVO(productTwo);
    }

    @Test
    public void updateProductWhenProductCodeIsNullShouldThrowException(){
        ProductVo productVO = new ProductVo();
        InvalidProductCodeException invalidProductCodeException = catchThrowableOfType(() -> productService.updateProduct(productVO, 1L), InvalidProductCodeException.class);
//        try {
//            productService.updateProduct(productVO, 1L);
//        } catch (InvalidProductCodeException e) {
//            assert true;
//            return;
//        }
//        assert false;
    }

    @Test
    public void updateProductWhenProductCodeIsInvalidShouldThrowException(){
        ProductVo productVO = new ProductVo();
        productVO.setCode("asd");

        try {
            productService.updateProduct(productVO, 1L);
        } catch (InvalidProductCodeException e) {
            assert true;
            return;
        }
        assert false;
    }

    @Test
    public void updateProductWhenProductCodeIsValidShouldUpdateTheProduct() throws InvalidProductCodeException {
        ProductVo productVO = new ProductVo();
        productVO.setCode("a new Code");
        productVO.setDescription("a new description");

        Product product = new Product();
        product.setCode("aCode");
        product.setDescription("an old description");
        when(productRepository.findByCode(any())).thenReturn(Optional.of(product));

        productService.updateProduct(productVO, 1L);

        verify(productRepository).findByCode(productVO.getCode());
        ArgumentCaptor<Product> productArgumentCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productArgumentCaptor.capture());

        Product productSendAsCapture = productArgumentCaptor.getValue();
        // aici se afla obiectul trimis ca parametru

        assertThat(productSendAsCapture.getDescription()).isEqualTo(productVO.getDescription());
    }

    @Test
    public void deleteProductWhenCodeIsNullShouldThrowAnException(){
        try {
            productService.deleteProduct(null, 1L);
        } catch (InvalidProductCodeException e) {
            assert true;
            return;
        }
        assert false;
    }

    @Test
    public void deleteProductWhenCodeIsValidShouldDeleteTheProduct() throws InvalidProductCodeException {
        Product product = new Product();
        product.setCode("aCode");
        when(productRepository.findByCode(any())).thenReturn(Optional.of(product));

        productService.deleteProduct("aCode", 1L);

        verify(productRepository).findByCode("aCode");
        verify(productRepository).delete(product);

    }

}