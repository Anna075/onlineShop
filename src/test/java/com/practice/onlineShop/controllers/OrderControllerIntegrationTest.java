package com.practice.onlineShop.controllers;

import com.practice.onlineShop.UtilsComponent;
import com.practice.onlineShop.entities.OrderItem;
import com.practice.onlineShop.entities.Orders;
import com.practice.onlineShop.entities.Product;
import com.practice.onlineShop.entities.User;
import com.practice.onlineShop.enums.Roles;
import com.practice.onlineShop.repositories.OrderRepository;
import com.practice.onlineShop.vos.OrderVO;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import static org.assertj.core.api.Assertions.assertThat;

import javax.transaction.Transactional;
import java.util.*;

import static com.practice.onlineShop.UtilsComponent.LOCALHOST;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpEntity.EMPTY;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class OrderControllerIntegrationTest {

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
    private TestRestTemplate testRestTemplate;

    @Autowired
    private RestTemplate restTemplateForPatch;

    @Autowired
    private UtilsComponent utilsComponent;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @Transactional
    public void addOrderWhenOrderIsValidShouldAddItToDB(){
        User user = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Product product = utilsComponent.storedTwoProductsInDB("code1", "code2");

        OrderVO orderVO = createOrderVO(user, product);

        testRestTemplate.postForEntity(LOCALHOST + port + "/order", orderVO, Void.class);

        List<Orders> ordersIterable = (List<Orders>) orderRepository.findAll();

        //order1 -> orderItems1 -> 1,2,3
        //order2 -> orderItems2 -> 3,4
        //List(orderItem1), List(orderItem2) -> List(orderItem1, orderItem2)
        // cautam acel item care contine produsul salvat in BD
        Optional<OrderItem> orderItemOptional = ordersIterable.stream()
                .map(order -> ((List<OrderItem>)order.getOrderItems()))
                .flatMap(List::stream)
                .filter(orderItem -> orderItem.getProduct().getId() == product.getId())
                .findFirst();
        assertThat(orderItemOptional).isPresent();
    }

    @Test
    public void whenRequestIsMadeByAdminShouldThrowException(){
        User user = utilsComponent.saveUserWithRole(Roles.ADMIN);
        Product product = utilsComponent.storedTwoProductsInDB("code1ForAdmin", "code2ForAdmin");

        OrderVO orderVO = createOrderVO(user, product);

        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity(LOCALHOST + port + "/order", orderVO, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(responseEntity.getBody()).isEqualTo("Utilizatorul nu are permisiunea de a executa aceasta operatiune!");
    }

    @Test
    public void whenRequestIsMadeByExpeditorShouldThrowException(){
        User user = utilsComponent.saveUserWithRole(Roles.EXPEDITOR);
        Product product = utilsComponent.storedTwoProductsInDB("code1ForExpeditor", "code2ForExpeditor");

        OrderVO orderVO = createOrderVO(user, product);

        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity(LOCALHOST + port + "/order", orderVO, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(responseEntity.getBody()).isEqualTo("Utilizatorul nu are permisiunea de a executa aceasta operatiune!");
    }

    @Test
    public void deliverWhenHavingAnOrderWhichIsNotCanceledShouldDeliverItByExpeditor(){
        User expeditor = utilsComponent.saveUserWithRole(Roles.EXPEDITOR);
        User client = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Product product = utilsComponent.storedTwoProductsInDB("code1ForExpeditorForDeliver", "code2ForExpeditorForDeliver");


        Orders orderWithProducts = utilsComponent.generateOrderItems(product, client);
        orderRepository.save(orderWithProducts);

        restTemplateForPatch.exchange(LOCALHOST + port + "/order/" + orderWithProducts.getId() + "/" + expeditor.getId(),
                PATCH, EMPTY, Void.class);

        Orders orderFromDB = orderRepository.findById(orderWithProducts.getId()).get();

        assertThat(orderFromDB.isDelivered()).isTrue();
    }

    @Test
    public void deliverWhenHavingAnOrderWhichIsNotCanceledShouldNOTdDeliverItByAdmin(){
        User adminAsExpeditor = utilsComponent.saveUserWithRole(Roles.ADMIN);
        User client = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Product product = utilsComponent.storedTwoProductsInDB("code1ForExpeditorForDeliverWhenAdmin", "code2ForExpeditorForDeliverWhenAdmin");


        Orders orderWithProducts = utilsComponent.generateOrderItems(product, client);
        orderRepository.save(orderWithProducts);

        try{
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/" + orderWithProducts.getId() + "/" + adminAsExpeditor.getId(),
                    PATCH, EMPTY, String.class);
        } catch(RestClientException restClientException){
            assertThat(restClientException.getMessage()).isEqualTo("400 : [Utilizatorul nu are permisiunea de a executa aceasta operatiune!]");
        }
    }

    @Test
    public void deliverWhenHavingAnOrderWhichIsNotCanceledShouldNOTdDeliverItByClient(){
        User clientAsExpeditor = utilsComponent.saveUserWithRole(Roles.CLIENT);
        User client = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Product product = utilsComponent.storedTwoProductsInDB("code1ForExpeditorForDeliverWhenClient", "code2ForExpeditorForDeliverWhenClient");


        Orders orderWithProducts = utilsComponent.generateOrderItems(product, client);
        orderRepository.save(orderWithProducts);

        try{
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/" + orderWithProducts.getId() + "/" + clientAsExpeditor.getId(),
                    PATCH, EMPTY, String.class);
        } catch(RestClientException restClientException){
            assertThat(restClientException.getMessage()).isEqualTo("400 : [Utilizatorul nu are permisiunea de a executa aceasta operatiune!]");
        }
    }


    @Test
    public void deliverWhenHavingAnOrderWhichIsCanceledShouldThrowAnException(){
        User expeditor = utilsComponent.saveUserWithRole(Roles.EXPEDITOR);
        Product product = utilsComponent.storedTwoProductsInDB("code1ForExpeditorForCanceledOrder", "code2ForExpeditorForCanceledOrder");
        User client = utilsComponent.saveUserWithRole(Roles.CLIENT);

        Orders orderWithProducts = utilsComponent.generateOrderItems(product, client);
        orderWithProducts.setCanceled(true);
        orderRepository.save(orderWithProducts);

        try{
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/" + orderWithProducts.getId() + "/" + expeditor.getId(),
                    PATCH, EMPTY, String.class);
        } catch(RestClientException restClientException){
            assertThat(restClientException.getMessage()).isEqualTo("400 : [Comanda a fost anulata!]");
        }
    }

    @Test
    public void cancelWhenValidOrderShouldCancelIt(){
        User client = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Product product = utilsComponent.storedTwoProductsInDB("code1ForCanceledOrder1", "code2ForCanceledOrder2");

        Orders orderWithProducts = utilsComponent.generateOrderItems(product, client);
        orderRepository.save(orderWithProducts);

        restTemplateForPatch.exchange(LOCALHOST + port + "/order/cancel/" + orderWithProducts.getId() + "/" + client.getId(),
                PATCH, EMPTY, Void.class);

        Orders orderFromDB = orderRepository.findById(orderWithProducts.getId()).get();

        assertThat(orderFromDB.isCanceled()).isTrue();
    }

    @Test
    public void cancelWhenOrderIsAlreadySentShouldThrowAnException(){
        User client = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Product product = utilsComponent.storedTwoProductsInDB("code1ForCanceledOrder12", "code2ForCanceledOrder21");

        Orders orderWithProducts = utilsComponent.generateOrderItems(product, client);
        orderRepository.save(orderWithProducts);

        try{
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/cancel/" + orderWithProducts.getId() + "/" + client.getId(),
                    PATCH, EMPTY, Void.class);
        } catch(RestClientException restClientException){
            assertThat(restClientException.getMessage()).isEqualTo("400 : [Comanda deja a fost livrata!]");
        }
    }

    @Test
    public void cancelWhenUserIsAdminShouldThrowAnException(){
        User admin = utilsComponent.saveUserWithRole(Roles.ADMIN);
        Product product = utilsComponent.storedTwoProductsInDB("code1ForCanceledOrderByAdmin12", "code2ForCanceledOrderByAdmin21");
        Orders orderWithProducts = utilsComponent.generateOrderItems(product, admin);
        orderRepository.save(orderWithProducts);

        try{
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/cancel/" + orderWithProducts.getId() + "/" + admin.getId(),
                    PATCH, EMPTY, Void.class);
        } catch(RestClientException restClientException){
            assertThat(restClientException.getMessage()).isEqualTo("400 : [Utilizatorul nu are permisiunea de a executa aceasta operatiune!]");
        }
    }

    @Test
    public void cancelWhenUserIsExpeditorShouldThrowAnException(){
        User expeditor = utilsComponent.saveUserWithRole(Roles.EXPEDITOR);
        Product product = utilsComponent.storedTwoProductsInDB("code1ForCanceledOrderByExpeditor1", "code2ForCanceledOrderByExpeditor2");
        Orders orderWithProducts = utilsComponent.generateOrderItems(product, expeditor);
        orderRepository.save(orderWithProducts);

        try{
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/cancel/" + orderWithProducts.getId() + "/" + expeditor.getId(),
                    PATCH, EMPTY, Void.class);
        } catch(RestClientException restClientException){
            assertThat(restClientException.getMessage()).isEqualTo("400 : [Utilizatorul nu are permisiunea de a executa aceasta operatiune!]");
        }
    }

    @Test
    @Transactional
    public void returnWhenOrderValidShouldReturnIt(){
        User client = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Product product = utilsComponent.storedTwoProductsInDB("productForReturn1", "productForReturn2");
        Orders orderWithProducts = utilsComponent.saveDeliveredOrders(client, product);

        restTemplateForPatch.exchange(LOCALHOST + port + "/order/return/" + orderWithProducts.getId() + "/" + client.getId(),
                PATCH, EMPTY, Void.class);

        Orders orderFromDB = orderRepository.findById(orderWithProducts.getId()).get();
        assertThat(orderFromDB.isReturned()).isTrue();
        assertThat(orderFromDB.getOrderItems().get(0).getProduct().getStock()).isEqualTo(product.getStock()
                + orderWithProducts.getOrderItems().get(0).getQuantity());
    }


    @Test
    @Transactional
    public void returnWhenOrderIsNotDeliveredShouldThrowException(){

        User client = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Product product = utilsComponent.storedTwoProductsInDB("productForReturn5", "productForReturn8");
        Orders orderWithProducts = utilsComponent.saveOrders(client, product);

        try{
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/return/" + orderWithProducts.getId() + "/" + client.getId(),
                    PATCH, EMPTY, Void.class);
        } catch(RestClientException restClientException){
            assertThat(restClientException.getMessage()).isEqualTo("400 : [Comanda nu poate fi returnata deoarece nu a fost livrata!]");
        }
    }

    @Test
    public void returnWhenOrderIsCanceledShouldThrowException(){
        User client = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Product product = utilsComponent.storedTwoProductsInDB("productForReturn28", "productForReturn39");
        Orders orderWithProducts = utilsComponent.saveCanceledAndDeliveredOrders(client, product);

        try{
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/return/" + orderWithProducts.getId() + "/" + client.getId(),
                    PATCH, EMPTY, Void.class);
        } catch(RestClientException restClientException){
            assertThat(restClientException.getMessage()).isEqualTo("400 : [Comanda a fost anulata!]");
        }
    }

    @Test
    public void returnWhenUserIsAdminShouldThrowException(){
        User adminAsClient = utilsComponent.saveUserWithRole(Roles.ADMIN);
        Product product = utilsComponent.storedTwoProductsInDB("productForReturn31", "productForReturn22");
        Orders orderWithProducts = utilsComponent.saveDeliveredOrders(adminAsClient, product);

        try{
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/return/" + orderWithProducts.getId() + "/" + adminAsClient.getId(),
                    PATCH, EMPTY, Void.class);
        } catch(RestClientException restClientException){
            assertThat(restClientException.getMessage()).isEqualTo("400 : [Utilizatorul nu are permisiunea de a executa aceasta operatiune!]");
        }
    }

    @Test
    public void returnWhenUserIsExpeditorShouldThrowException(){
        User expeditorAsClient = utilsComponent.saveUserWithRole(Roles.EXPEDITOR);
        Product product = utilsComponent.storedTwoProductsInDB("productForReturn14", "productForReturn24");
        Orders orderWithProducts = utilsComponent.saveDeliveredOrders(expeditorAsClient, product);

        try{
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/return/" + orderWithProducts.getId() + "/" + expeditorAsClient.getId(),
                    PATCH, EMPTY, Void.class);
        } catch(RestClientException restClientException){
            assertThat(restClientException.getMessage()).isEqualTo("400 : [Utilizatorul nu are permisiunea de a executa aceasta operatiune!]");
        }
    }

    private OrderVO createOrderVO(User user, Product product) {
        OrderVO orderVO = new OrderVO();
        orderVO.setUserId((int) user.getId());
        Map<Integer, Integer> orderMap = new HashMap<>();
        orderMap.put((int) product.getId(), 1);
        orderVO.setProductIdsToQuantity(orderMap);
        return orderVO;
    }
}