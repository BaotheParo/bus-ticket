package com.long_bus_distance.tickets.services;

import com.long_bus_distance.tickets.dto.UserUpdateRequest;
import com.long_bus_distance.tickets.entity.User;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class VNPayService {
    private static final String VNP_PAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final String VNP_TMN_CODE = "24JRS5CL"; // Mã Website giả lập
    private static final String VNP_HASH_SECRET = "NVX085JF089X08X08X08"; // Secret giả lập
    private static final String VNP_RETURN_URL = "http://localhost:8080/api/v1/payment/vnpay-return"; // URL gọi về khi
                                                                                                      // xong

    public String createPaymentUrl(String orderGroupId, double totalAmount) {
        String vnp_TxnRef = orderGroupId;
        long amount = (long) (totalAmount * 100); // VNPay tính đơn vị đồng * 100

        // Tạo URL giả để test flow trên local
        return "http://localhost:8080/api/v1/payment/vnpay-return?" +
                "vnp_TxnRef=" + vnp_TxnRef +
                "&vnp_ResponseCode=00" + // 00 là thành công
                "&vnp_Amount=" + amount +
                "&vnp_OrderInfo=Thanh+toan+nhom+ve";
    }

    public static interface UserService {
        User updateUser(UUID id, UserUpdateRequest request);
    }
}