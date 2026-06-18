package com.fernirx.sneakerapi.order.service.impl;

import com.fernirx.sneakerapi.order.repository.OrderRepository;
import com.fernirx.sneakerapi.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
}
