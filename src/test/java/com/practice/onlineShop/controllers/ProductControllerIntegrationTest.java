package com.practice.onlineShop.controllers;

import com.practice.onlineShop.entities.Address;
import com.practice.onlineShop.entities.Product;
import com.practice.onlineShop.entities.User;
import com.practice.onlineShop.enums.Currencies;
import com.practice.onlineShop.enums.Roles;
import com.practice.onlineShop.repositories.ProductRepository;
import com.practice.onlineShop.repositories.UserRepository;
import com.practice.onlineShop.vos.ProductVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import com.practice.onlineShop.UtilsComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static com.practice.onlineShop.UtilsComponent.LOCALHOST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpEntity.EMPTY;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductControllerIntegrationTest {

    @TestConfiguration
    static class ProductControllerIntegrationTestContextConfiguration{
        @Bean
        public RestTemplate restTemplateForPatch(){
            return new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        }
    }

    @LocalServerPort
    private int port;

    @Autowired
    private ProductController productController;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private RestTemplate restTemplateForPatch;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UtilsComponent utilsComponent;

    @Test
    public void contextLoads(){
        assertThat(productController).isNotNull();
    }

    @Test
    public void addProductWhenUserIsAdminShouldStoreTheProduct(){
        productRepository.deleteAll();

        User userEntity = new User();
        userEntity.setFirstname("adminFirstName");
        Collection<Roles> roles = new ArrayList<>();
        roles.add(Roles.ADMIN);
        userEntity.setRoles(roles);
        Address address = new Address();
        address.setCity("Bucuresti");
        address.setStreet("AWonderfulStreet");
        address.setNumber(2);
        address.setZipcode("123");
        userEntity.setAddress(address);
        userRepository.save(userEntity);


        ProductVo productVO = new ProductVo();
        productVO.setCode("aProductCode");
        productVO.setPrice(100);
        productVO.setCurrency(Currencies.RON);
        productVO.setStock(12);
        productVO.setDescription("A product description");
        productVO.setValid(true);

        testRestTemplate.postForEntity(LOCALHOST + port + "/product/" + userEntity.getId(), productVO, Void.class);

        Iterable<Product> products = productRepository.findAll();
        assertThat(products).hasSize(1);

        Product product = products.iterator().next();
        assertThat(product.getCode()).isEqualTo(productVO.getCode());
    }

    @Test
    public void addProductWhenUserIsNotInDBShouldThrowInvalidCustomerIdException(){
        ProductVo productVO = new ProductVo();
        productVO.setCode("aProductCode");
        productVO.setPrice(100);
        productVO.setCurrency(Currencies.RON);
        productVO.setStock(12);
        productVO.setDescription("A product description");
        productVO.setValid(true);

        ResponseEntity<String> response
                = testRestTemplate.postForEntity(LOCALHOST + port + "/product/123", productVO, String.class);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Comanda dvs. nu este asignata unui user valid!");

    }

    @Test
    public void addProductWhenUserIsNOTAdminShouldThrowInvalidOperationException(){
        User userEntity = utilsComponent.saveUserWithRole(Roles.CLIENT);


        ProductVo productVO = new ProductVo();
        productVO.setCode("aProductCode");
        productVO.setPrice(100);
        productVO.setCurrency(Currencies.RON);
        productVO.setStock(12);
        productVO.setDescription("A product description");
        productVO.setValid(true);

        ResponseEntity<String> response
                = testRestTemplate.postForEntity(LOCALHOST+ port + "/product/" + userEntity.getId(), productVO, String.class);


        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Utilizatorul nu are permisiunea de a executa aceasta operatiune!");
    }

    @Test
    public void getProductByCodeWhenCodeIsPresentInDBShouldReturnTheProduct(){
        Product product = utilsComponent.storedTwoProductsInDB("aWonderfulCode", "anotherCode");

        ProductVo productResponse = testRestTemplate.getForObject(LOCALHOST + port + "/product/"+product.getCode(), ProductVo.class);
        assertThat(productResponse.getCode()).isEqualTo(product.getCode());
    }


    @Test
    public void getProductByCodeShouldReturnErrorMessageWhenProductCodeIsNotPresent(){
        String response = testRestTemplate.getForObject(LOCALHOST + port + "/product/123432", String.class);
        assertThat(response).isEqualTo("Codul produsului trimis este invalid!");
    }

    @Test
    public void getProducts(){
        productRepository.deleteAll();
        utilsComponent.storedTwoProductsInDB("aProductCode", "aProductCode2");

        ProductVo[] products = testRestTemplate.getForObject(LOCALHOST + port + "/product", ProductVo[].class);

        assertThat(products).hasSize(2);
        assertThat(products[0].getCode()).contains("aProductCode");
        assertThat(products[1].getCode()).contains("aProductCode2");
    }

    @Test
    public void updateProductWhenUserIsEditorShouldUpdateTheProduct(){
        Product product = utilsComponent.generateProduct("aProduct");
        productRepository.save(product);

        User user = utilsComponent.saveUserWithRole(Roles.EDITOR);
        ProductVo productVo = new ProductVo();
        productVo.setCode(product.getCode());
        productVo.setCurrency(Currencies.EUR);
        productVo.setPrice(200L);
        productVo.setStock(100);
        productVo.setDescription("anotherDescription");
        productVo.setValid(false);
        testRestTemplate.put(LOCALHOST + port + "/product/" + user.getId(), productVo);

        Optional<Product> updateProduct = productRepository.findByCode(productVo.getCode());

        assertThat(updateProduct.get().getDescription()).isEqualTo(productVo.getDescription());
        assertThat(updateProduct.get().getCurrency()).isEqualTo(productVo.getCurrency());
        assertThat(updateProduct.get().getPrice()).isEqualTo(productVo.getPrice());
        assertThat(updateProduct.get().getStock()).isEqualTo(productVo.getStock());
        assertThat(updateProduct.get().isValid()).isEqualTo(productVo.isValid());
    }

    @Test
    public void updateProductWhenUserIsAdminShouldUpdateTheProduct(){
        Product product = utilsComponent.generateProduct("aProduct");
        productRepository.save(product);

        User user = utilsComponent.saveUserWithRole(Roles.ADMIN);
        ProductVo productVo = new ProductVo();
        productVo.setCode(product.getCode());
        productVo.setCurrency(Currencies.EUR);
        productVo.setPrice(200L);
        productVo.setStock(100);
        productVo.setDescription("anotherDescription");
        productVo.setValid(false);
        testRestTemplate.put(LOCALHOST + port + "/product/" + user.getId(), productVo);

        Optional<Product> updateProduct = productRepository.findByCode(productVo.getCode());

        assertThat(updateProduct.get().getDescription()).isEqualTo(productVo.getDescription());
        assertThat(updateProduct.get().getCurrency()).isEqualTo(productVo.getCurrency());
        assertThat(updateProduct.get().getPrice()).isEqualTo(productVo.getPrice());
        assertThat(updateProduct.get().getStock()).isEqualTo(productVo.getStock());
        assertThat(updateProduct.get().isValid()).isEqualTo(productVo.isValid());
    }

    @Test
    public void updateProductWhenUserIsClientShouldNOTUpdateTheProduct(){
        Product product = utilsComponent.generateProduct("aProduct100");
        productRepository.save(product);

        User user = utilsComponent.saveUserWithRole(Roles.CLIENT);
        ProductVo productVo = new ProductVo();
        productVo.setCode(product.getCode());
        productVo.setCurrency(Currencies.EUR);
        productVo.setPrice(200L);
        productVo.setStock(100);
        productVo.setDescription("anotherDescription");
        productVo.setValid(false);
        testRestTemplate.put(LOCALHOST + port + "/product/" + user.getId(), productVo);

        Optional<Product> updateProduct = productRepository.findByCode(productVo.getCode());

        assertThat(updateProduct.get().getDescription()).isEqualTo(product.getDescription());
        assertThat(updateProduct.get().getCurrency()).isEqualTo(product.getCurrency());
        assertThat(updateProduct.get().getPrice()).isEqualTo(product.getPrice());
        assertThat(updateProduct.get().getStock()).isEqualTo(product.getStock());
        assertThat(updateProduct.get().isValid()).isEqualTo(product.isValid());
    }

    @Test
    public void deleteProductWhenUserIsAdminShouldDeleteThatProduct(){
        Product product = utilsComponent.generateProduct("aProductForDelete");
        productRepository.save(product);

        testRestTemplate.delete(LOCALHOST + port + "/product/" + product.getCode() + "/1");

        assertThat(productRepository.findByCode(product.getCode())).isNotPresent();
    }

    @Test
    public void deleteProductWhenUserIsClientShouldNotDeleteThatProduct(){
        Product product = utilsComponent.generateProduct("aProductForDelete");
        productRepository.save(product);

        testRestTemplate.delete(LOCALHOST + port + "/product/" + product.getCode() + "/2");

        assertThat(productRepository.findByCode(product.getCode())).isPresent();
    }

    @Test
    public void addStockWhenAddingStockToAnItemShouldBeSavedInDB(){
        Product product = utilsComponent.generateProduct("aProductForAddingStock");
        productRepository.save(product);

        User user = utilsComponent.saveUserWithRole(Roles.ADMIN);

        restTemplateForPatch.exchange(LOCALHOST + port + "/product/" + product.getCode() + "/3/" + user.getId(),
                PATCH, EMPTY, Void.class);

        Product productFromDB = productRepository.findByCode(product.getCode()).get();
        assertThat(productFromDB.getStock()).isEqualTo(4);
    }
}