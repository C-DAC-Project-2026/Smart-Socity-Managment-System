package com.society.serviceimpl;

import com.razorpay.*;
import com.society.dto.RazorpayDTOs.*;
import com.society.entity.MaintenanceBill;
import com.society.entity.Payment;
import com.society.exception.BadRequestException;
import com.society.exception.ResourceNotFoundException;
import com.society.repository.MaintenanceBillRepository;
import com.society.repository.PaymentRepository;
import com.society.service.NotificationService;
import com.society.service.RazorpayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class RazorpayServiceImpl implements RazorpayService {

    private final RazorpayClient            razorpayClient;
    private final MaintenanceBillRepository billRepository;
    private final PaymentRepository         paymentRepository;
    private final NotificationService       notificationService;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Value("${razorpay.currency}")
    private String currency;

    // ── STEP 1: Create Razorpay Order ────────────────────────────────
    @Override
    public CreateOrderResponse createOrder(Long billId) {
        MaintenanceBill bill = billRepository.findById(billId)
            .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + billId));

        if (bill.getStatus() == MaintenanceBill.BillStatus.PAID) {
            throw new BadRequestException("Bill is already paid.");
        }

        if (paymentRepository.findByBill_BillId(billId).isPresent()) {
            throw new BadRequestException("Payment already exists for this bill.");
        }

        try {
            // Razorpay amount is in PAISE (multiply ₹ by 100)
            long amountInPaise = bill.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount",   amountInPaise);
            orderRequest.put("currency", currency);
            orderRequest.put("receipt",  "bill_" + billId);
            orderRequest.put("payment_capture", 1);  // auto-capture

            // Optional: prefill resident info
            JSONObject notes = new JSONObject();
            notes.put("bill_id",       billId);
            notes.put("resident_name", bill.getResident().getUser().getName());
            notes.put("flat_no",       bill.getResident().getFlatNo());
            orderRequest.put("notes", notes);

            Order order = razorpayClient.orders.create(orderRequest);
            log.info("Razorpay order created: {} for bill: {}", order.get("id"), billId);

            return CreateOrderResponse.builder()
                .orderId(order.get("id"))
                .amount(amountInPaise)
                .currency(currency)
                .keyId(keyId)
                .residentName(bill.getResident().getUser().getName())
                .description("Maintenance Bill - " + getMonthName(bill.getMonth()) + " " + bill.getYear())
                .build();

        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay order: {}", e.getMessage());
            throw new BadRequestException("Payment gateway error: " + e.getMessage());
        }
    }

    // ── STEP 2: Verify Signature + Save Payment ───────────────────────
    @Override
    @Transactional
    public boolean verifyAndSavePayment(PaymentVerifyRequest req) {
        // 1. Verify HMAC SHA256 signature
        boolean valid = verifySignature(req.getRazorpayOrderId(), req.getRazorpayPaymentId(), req.getRazorpaySignature());

        if (!valid) {
            log.error("Razorpay signature verification FAILED for order: {}", req.getRazorpayOrderId());
            throw new BadRequestException("Payment verification failed — invalid signature.");
        }

        // 2. Fetch the bill
        MaintenanceBill bill = billRepository.findById(req.getBillId())
            .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + req.getBillId()));

        // 3. Save payment record
        Payment payment = Payment.builder()
            .bill(bill)
            .amount(bill.getAmount())
            .paymentMode(resolvePaymentMode(req.getPaymentMode()))
            .transactionId(req.getRazorpayPaymentId())
            .status(Payment.PaymentStatus.SUCCESS)
            .build();
        paymentRepository.save(payment);

        // 4. Mark bill as PAID
        bill.setStatus(MaintenanceBill.BillStatus.PAID);
        billRepository.save(bill);

        // 5. Notify resident
        notificationService.sendNotification(
            bill.getResident().getUser().getUserId(),
            "Payment of ₹" + bill.getAmount() + " for " + getMonthName(bill.getMonth())
                + " " + bill.getYear() + " successful! TxnId: " + req.getRazorpayPaymentId(),
            "PAYMENT"
        );

        log.info("Payment saved successfully. Bill: {}, TxnId: {}", req.getBillId(), req.getRazorpayPaymentId());
        return true;
    }

    // ── Signature Verification ────────────────────────────────────────
    private boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String generated = HexFormat.of().formatHex(hash);
            return generated.equals(signature);
        } catch (Exception e) {
            log.error("Signature verification error: {}", e.getMessage());
            return false;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────
    private Payment.PaymentMode resolvePaymentMode(String mode) {
        if (mode == null) return Payment.PaymentMode.ONLINE;
        return switch (mode.toUpperCase()) {
            case "UPI"        -> Payment.PaymentMode.UPI;
            case "NEFT", "NETBANKING" -> Payment.PaymentMode.NEFT;
            case "CASH"       -> Payment.PaymentMode.CASH;
            case "CHEQUE"     -> Payment.PaymentMode.CHEQUE;
            default           -> Payment.PaymentMode.ONLINE;
        };
    }

    private String getMonthName(int month) {
        String[] names = {"","January","February","March","April","May","June",
                          "July","August","September","October","November","December"};
        return (month >= 1 && month <= 12) ? names[month] : "Month " + month;
    }
}
