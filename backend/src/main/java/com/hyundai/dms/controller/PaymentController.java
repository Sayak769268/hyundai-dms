package com.hyundai.dms.controller;

import com.hyundai.dms.dto.PaymentDto;
import com.hyundai.dms.service.impl.PaymentServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentServiceImpl paymentService;

    @GetMapping("/order/{salesOrderId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DEALER')")
    public ResponseEntity<List<PaymentDto>> getPaymentsByOrder(@PathVariable Long salesOrderId) {
        return ResponseEntity.ok(paymentService.getPaymentsBySalesOrder(salesOrderId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PROCESS_SALES') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<PaymentDto> processPayment(@RequestBody PaymentDto dto) {
        return new ResponseEntity<>(paymentService.processPayment(dto), HttpStatus.CREATED);
    }
}
