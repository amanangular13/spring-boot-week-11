package com.aman.inventory_service.service;

import com.aman.inventory_service.dto.OrderRequestDto;
import com.aman.inventory_service.dto.OrderRequestItemDto;
import com.aman.inventory_service.dto.ProductDto;
import com.aman.inventory_service.entity.Product;
import com.aman.inventory_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    public List<ProductDto> getAllInventory() {
        log.info("Fetching all inventory items");
        List<Product> inventories = productRepository.findAll();
        return inventories.stream()
                .map(product -> modelMapper.map(product, ProductDto.class))
                .collect(Collectors.toList());
    }

    public ProductDto getProductById(Long id) {
        log.info("Fetching product with id: {}", id);
        Product product = productRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Product not found"));
        return modelMapper.map(product, ProductDto.class);
    }

    @Transactional
    public Double reduceStocks(OrderRequestDto orderRequestDto) {
        log.info("Reducing the stocks");
        Double totalPrice = 0.0;
        for(OrderRequestItemDto orderRequestItemDto: orderRequestDto.getItems()) {
            Long productId = orderRequestItemDto.getProductId();
            Integer quantity = orderRequestItemDto.getQuantity();

            Product product = productRepository.findById(productId).orElseThrow(() ->
                    new RuntimeException("product not found"));

            if(product.getStock() < quantity) {
                throw new RuntimeException("Insufficient items in stock");
            }

            totalPrice += quantity * product.getPrice();
            product.setStock(product.getStock() - quantity);
            productRepository.save(product);
        }
        return totalPrice;
    }
}
