package com.society.serviceimpl;

import com.society.dto.PaymentDTO;
import com.society.entity.MaintenanceBill;
import com.society.entity.Payment;
import com.society.exception.BadRequestException;
import com.society.exception.ResourceNotFoundException;
import com.society.repository.MaintenanceBillRepository;
import com.society.repository.PaymentRepository;
import com.society.service.NotificationService;
import com.society.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final MaintenanceBillRepository billRepository;
    private final NotificationService notificationService;

    @Override @Transactional
    public PaymentDTO makePayment(PaymentDTO dto) {
        MaintenanceBill bill = billRepository.findById(dto.getBillId())
            .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + dto.getBillId()));
        if (paymentRepository.findByBill_BillId(dto.getBillId()).isPresent())
            throw new BadRequestException("Payment already made for bill: " + dto.getBillId());
        String txnId = dto.getTransactionId() != null ? dto.getTransactionId() : "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Payment payment = Payment.builder().bill(bill).amount(bill.getAmount())
            .paymentMode(Payment.PaymentMode.valueOf(dto.getPaymentMode()))
            .transactionId(txnId).status(Payment.PaymentStatus.SUCCESS).build();
        payment = paymentRepository.save(payment);
        bill.setStatus(MaintenanceBill.BillStatus.PAID);
        billRepository.save(bill);
        notificationService.sendNotification(bill.getResident().getUser().getUserId(),
            "Payment of ₹" + bill.getAmount() + " for " + bill.getMonth() + "/" + bill.getYear() + " successful. TxnId: " + txnId, "PAYMENT");
        return toDTO(payment);
    }
    @Override public Page<PaymentDTO> getAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(this::toDTO);
    }
    @Override public Page<PaymentDTO> getPaymentsByResident(Long residentId, Pageable pageable) {
        return paymentRepository.findByBill_Resident_ResidentId(residentId, pageable).map(this::toDTO);
    }
    @Override public PaymentDTO getPaymentById(Long id) {
        return toDTO(paymentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id)));
    }
    private PaymentDTO toDTO(Payment p) {
        return PaymentDTO.builder().paymentId(p.getPaymentId()).billId(p.getBill().getBillId())
            .amount(p.getAmount()).paymentDate(p.getPaymentDate()).paymentMode(p.getPaymentMode().name())
            .transactionId(p.getTransactionId()).status(p.getStatus().name())
            .residentName(p.getBill().getResident().getUser().getName())
            .flatNo(p.getBill().getResident().getFlatNo())
            .billMonth(p.getBill().getMonth()).billYear(p.getBill().getYear()).build();
    }
}
