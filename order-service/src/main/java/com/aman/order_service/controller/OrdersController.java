package com.aman.order_service.controller;

import com.aman.order_service.dto.OrderRequestDto;
import com.aman.order_service.service.OrdersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/orders")
@Slf4j
public class OrdersController {

    private final OrdersService ordersService;

    @GetMapping(path = "/helloOrders")
    public String helloOrders() {
        return "Hello from order service";
    }

    @GetMapping
    public ResponseEntity<List<OrderRequestDto>> getAllOrders() {
        log.info("Fetching all orders via controllers");
        List<OrderRequestDto> orders = ordersService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<OrderRequestDto> getOrderById(@PathVariable Long id) {
        log.info("Fetching order with id: {} via controller", id);
        OrderRequestDto orderRequestDto = ordersService.getOrderById(id);
        return ResponseEntity.ok(orderRequestDto);
    }
}
