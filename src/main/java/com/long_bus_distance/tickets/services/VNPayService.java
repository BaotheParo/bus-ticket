package com.long_bus_distance.tickets.services;

import com.long_bus_distance.tickets.entity.Ticket;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayService {
    private static final String VNP_PAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final String VNP_TMN_CODE = "24JRS5CL"; // Mã Website giả lập
    private static final String VNP_HASH_SECRET = "NVX085JF089X08X08X08"; // Secret giả lập
    private static final String VNP_RETURN_URL = "http://localhost:8080/api/v1/payment/vnpay-return"; // URL gọi về khi xong

    public String createPaymentUrl(Ticket ticket) {
        String vnp_TxnRef = ticket.getId().toString();
        long amount = (long) (ticket.getPrice() * 100); // VNPay tính đơn vị đồng * 100

        // Tạo URL giả để test flow trên local
        return "http://localhost:8080/api/v1/payment/vnpay-return?" +
                "vnp_TxnRef=" + vnp_TxnRef +
                "&vnp_ResponseCode=00" + // 00 là thành công
                "&vnp_Amount=" + amount +
                "&vnp_OrderInfo=Thanh+toan+ve+xe";
    }
}