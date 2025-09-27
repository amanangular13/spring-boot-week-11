package com.aman.order_service.service;

import com.aman.order_service.clients.InventoryFeignClient;
import com.aman.order_service.dto.OrderRequestDto;
import com.aman.order_service.entity.OrderItem;
import com.aman.order_service.entity.OrderStatus;
import com.aman.order_service.entity.Orders;
import com.aman.order_service.repository.OrdersRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdersService {

    private final OrdersRepository ordersRepository;
    private final ModelMapper modelMapper;
    private final InventoryFeignClient inventoryFeignClient;

    public List<OrderRequestDto> getAllOrders() {
        log.info("Fetching all orders");
        List<Orders> orders = ordersRepository.findAll();
        return orders.stream().map(order -> modelMapper.map(order, OrderRequestDto.class)).collect(Collectors.toList());
    }

    public OrderRequestDto getOrderById(Long id) {
        log.info("Fetching order with id: {}", id);
        Orders order = ordersRepository.findById(id).orElseThrow(()-> new RuntimeException("order not found"));
        return modelMapper.map(order, OrderRequestDto.class);
    }

//    @Retry(name = "inventoryServiceRetry", fallbackMethod = "createOrderFallback")
    @RateLimiter(name = "inventoryServiceRateLimiter", fallbackMethod = "createOrderFallback")
    @CircuitBreaker(name = "inventoryCircuitBreaker", fallbackMethod = "createOrderFallback")
    public OrderRequestDto createOrder(OrderRequestDto orderRequestDto) {
        log.info("creating new order with details: {}", orderRequestDto);
        Double totalPrice = inventoryFeignClient.reduceStocks(orderRequestDto);
        Orders orders = modelMapper.map(orderRequestDto, Orders.class);
        for(OrderItem orderItem: orders.getItems()) {
            orderItem.setOrder(orders);
        }
        orders.setTotalPrice(totalPrice);
        orders.setOrderStatus(OrderStatus.CONFIRMED);

        Orders savedOrder = ordersRepository.save(orders);
        return modelMapper.map(savedOrder, OrderRequestDto.class);
    }

    public OrderRequestDto createOrderFallback(OrderRequestDto orderRequestDto, Throwable throwable) {
        log.error("Failed to create order, fallback occurred due to {}", throwable.getMessage());
        return new OrderRequestDto();
    }
}
