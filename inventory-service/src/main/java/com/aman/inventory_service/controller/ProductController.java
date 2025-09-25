package com.aman.inventory_service.controller;

import com.aman.inventory_service.dto.ProductDto;
import com.aman.inventory_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final DiscoveryClient discoveryClient;
    private final RestClient restClient;

    @GetMapping(path = "/fetchOrders")
    public String fetchFromOrderService() {
        ServiceInstance orderService = discoveryClient.getInstances("order-service").getFirst();

        return restClient.get()
                .uri(orderService.getUri()+"/orders/core/helloOrders")
                .retrieve()
                .body(String.class);
    }

    @GetMapping
    ResponseEntity<List<ProductDto>> getAllInventory() {
        List<ProductDto> inventories = productService.getAllInventory();
        return ResponseEntity.ok(inventories);
    }

    @GetMapping(path = "/{id}")
    ResponseEntity<ProductDto> getAllInventory(@PathVariable Long id) {
        ProductDto inventory = productService.getProductById(id);
        return ResponseEntity.ok(inventory);
    }


}
