package com.long_bus_distance.tickets.controller;

import com.long_bus_distance.tickets.services.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public ResponseEntity<String> paymentCallback(
            @RequestParam(name = "vnp_TxnRef") String ticketId,
            @RequestParam(name = "vnp_ResponseCode") String responseCode
            // Các param khác của VNPay: vnp_Amount, vnp_SecureHash...
    ) {
        log.info("Nhận callback thanh toán cho vé: {}, Code: {}", ticketId, responseCode);
        
        try {
            ticketService.processPaymentCallback(ticketId, responseCode);
            if ("00".equals(responseCode)) {
                return ResponseEntity.ok("Thanh toán thành công! Vui lòng kiểm tra vé trong phần Lịch sử.");
            } else {
                return ResponseEntity.badRequest().body("Thanh toán thất bại hoặc bị hủy.");
            }
        } catch (Exception e) {
            log.error("Lỗi xử lý thanh toán", e);
            return ResponseEntity.internalServerError().body("Lỗi hệ thống khi xử lý thanh toán.");
        }
    }
}