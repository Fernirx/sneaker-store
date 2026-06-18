package com.fernirx.sneakerapi.order.controller;

import com.fernirx.sneakerapi.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class InternalOrderController {
    private final OrderService orderService;
}
