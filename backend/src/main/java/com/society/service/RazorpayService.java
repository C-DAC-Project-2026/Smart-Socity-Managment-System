package com.society.service;

import com.society.dto.RazorpayDTOs.*;

public interface RazorpayService {
    // Creates a Razorpay order — called before showing payment popup
    CreateOrderResponse createOrder(Long billId);

    // Verifies signature after payment — called after user pays
    boolean verifyAndSavePayment(PaymentVerifyRequest request);
}
