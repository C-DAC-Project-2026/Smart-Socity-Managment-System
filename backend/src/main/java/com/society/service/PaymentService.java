package com.society.service;
import com.society.dto.PaymentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface PaymentService {
    PaymentDTO makePayment(PaymentDTO dto);
    Page<PaymentDTO> getAllPayments(Pageable pageable);
    Page<PaymentDTO> getPaymentsByResident(Long residentId, Pageable pageable);
    PaymentDTO getPaymentById(Long id);
}
