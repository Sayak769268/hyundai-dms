package com.hyundai.dms.service.impl;

import com.hyundai.dms.dto.PaymentDto;
import com.hyundai.dms.entity.Payment;
import com.hyundai.dms.entity.SalesOrder;
import com.hyundai.dms.exception.ResourceNotFoundException;
import com.hyundai.dms.repository.PaymentRepository;
import com.hyundai.dms.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl {

    private final PaymentRepository paymentRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final com.hyundai.dms.repository.UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<PaymentDto> getPaymentsBySalesOrder(Long salesOrderId) {
        return paymentRepository.findBySalesOrderId(salesOrderId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentDto processPayment(PaymentDto dto) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        com.hyundai.dms.entity.User currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        SalesOrder order = salesOrderRepository.findById(dto.getSalesOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found"));

        if (!isAdmin) {
            Long dealerId = currentUser.getDealerId();
            if (dealerId == null) {
                throw new org.springframework.security.access.AccessDeniedException("User is not associated with any dealership");
            }
            if (!order.getDealerId().equals(dealerId)) {
                throw new ResourceNotFoundException("Sales Order not found in your dealership");
            }
        }

        Payment payment = Payment.builder()
                .salesOrder(order)
                .amount(dto.getAmount())
                .paymentMode(dto.getPaymentMode())
                .transactionRef(dto.getTransactionRef())
                .paymentStatus(dto.getPaymentStatus() != null ? dto.getPaymentStatus() : "COMPLETED")
                .build();
        
        // Note: Real world would involve triggering invoice generation or order status updates here.
        if ("COMPLETED".equals(payment.getPaymentStatus())) {
             order.setStatus("INVOICED");
             salesOrderRepository.save(order);
        }

        return mapToDto(paymentRepository.save(payment));
    }

    private PaymentDto mapToDto(Payment p) {
        return PaymentDto.builder()
                .id(p.getId())
                .salesOrderId(p.getSalesOrder().getId())
                .amount(p.getAmount())
                .paymentMode(p.getPaymentMode())
                .transactionRef(p.getTransactionRef())
                .paymentStatus(p.getPaymentStatus())
                .paymentDate(p.getPaymentDate())
                .build();
    }
}
