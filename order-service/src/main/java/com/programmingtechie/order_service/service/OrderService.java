package com.programmingtechie.order_service.service;

import com.programmingtechie.order_service.dto.InventoryResponse;
import com.programmingtechie.order_service.dto.OrderLineItemsDto;
import com.programmingtechie.order_service.dto.OrderRequest;
import com.programmingtechie.order_service.event.OrderPlacedEvent;
import com.programmingtechie.order_service.model.Order;
import com.programmingtechie.order_service.model.OrderLineItems;
import com.programmingtechie.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final KafkaTemplate<String,OrderPlacedEvent> kafkaTemplate;

    public String placeOrder(OrderRequest orderRequest) throws IllegalAccessException {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = orderLineItems.stream().map(orderLineItem -> orderLineItem.getSkuCode()).toList();

        //Check if its in stock by calling the inventory service
        //by default webClient does asynchronous calls.. block is required
        //to have a synchorinous call..
        //body to mono part of reactive framework.. Boolean is type of response the api call gives
        InventoryResponse[] inventoryResponsesArray = webClientBuilder.build().get()
                        .uri("http://inventory-service/api/inventory",uriBuilder ->
                            uriBuilder.queryParam("skuCode",skuCodes).build()
                        )
                                .retrieve()
                                        .bodyToMono(InventoryResponse[].class)
                                                .block();

        boolean allProductsInStock = Arrays.stream(inventoryResponsesArray).allMatch(InventoryResponse::isInStock);

        if(allProductsInStock){
            orderRepository.save(order);
            kafkaTemplate.send("notificationTopic",new OrderPlacedEvent(order.getOrderNumber()));
            log.info("Order with ID {} saved to the database",order.getOrderNumber());
            return "Order Placed Successfully";
        }else{
            throw new IllegalAccessException("Products out of Stock.. Please try again after sometime!!");
        }
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
