package com.long_bus_distance.tickets.controller;

import com.long_bus_distance.tickets.services.TicketService;
import com.long_bus_distance.tickets.services.VNPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final TicketService ticketService;

    @GetMapping("/vnpay-return")
    public ResponseEntity<Void> paymentCallback(
            @RequestParam(name = "vnp_TxnRef") String orderId,
            @RequestParam(name = "vnp_ResponseCode") String responseCode,
            @RequestParam(name = "vnp_OrderInfo") String orderInfo
            // Các param khác của VNPay: vnp_Amount, vnp_SecureHash...
    ) {
        log.info("Nhận callback thanh toán cho vé: {}, Code: {}", orderId, responseCode);

        String frontendSuccessUrl = "https://localhost:3000/payment-result?status=success&orderId=" + orderId + "&orderInfo=" + orderInfo;
        String frontendFailedUrl = "https://localhost:3000/payment-result?status=failed&orderId=" + orderId + "&orderInfo=" + orderInfo;
        
        try {
            ticketService.processPaymentCallback(orderId, responseCode);
            if ("00".equals(responseCode)) {
                return ResponseEntity.status(302).header("Location", frontendSuccessUrl).build();
            } else {
                return ResponseEntity.status(302).header("Location", frontendFailedUrl).build();
            }
        } catch (Exception e) {
            log.error("Lỗi xử lý thanh toán", e);
            return ResponseEntity.status(302).header("Location", frontendFailedUrl).build();
        }
    }
}