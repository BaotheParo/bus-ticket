package com.long_bus_distance.tickets.services;

import com.long_bus_distance.tickets.dto.UserUpdateRequest;
import com.long_bus_distance.tickets.entity.User;
import org.springframework.stereotype.Service;

import java.util.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

@Service
public class VNPayService {
    private static final String VNP_PAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final String VNP_TMN_CODE = "KHMP8QCY"; // Mã Website giả lập
    private static final String VNP_HASH_SECRET = "NB9ZHDEAG8ON1N1XNQ28Q3MR31AADJ5A"; // Secret giả lập
    private static final String VNP_RETURN_URL = "http://localhost:8080/api/v1/payment/vnpay-return"; // URL gọi về khi// xon

    private String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final javax.crypto.Mac hmac512 = javax.crypto.Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    // Hàm tạo URL thanh toán chuẩn Sandbox
    public String createPaymentUrl(String orderGroupId, String tripId, double totalAmount) {
        // 1. Chuyển đổi số tiền: VNPay yêu cầu đơn vị VND * 100
        long amount = (long) (totalAmount * 100);

        // 2. Lấy các thông số cấu hình (từ static final constants bạn đã khai báo)
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = orderGroupId; // Mã tham chiếu giao dịch (Unique)
        String vnp_IpAddr = "127.0.0.1";  // IP của client, local để cứng cũng được
        String vnp_TmnCode = VNP_TMN_CODE;
        String orderType = "other";

        // 3. Tạo Map chứa các tham số
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");

        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        // Thông tin hiển thị trên cổng thanh toán
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang " + orderGroupId + " cho chuyen di " + tripId);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");

        // URL trả về sau khi thanh toán
        vnp_Params.put("vnp_ReturnUrl", VNP_RETURN_URL);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        // 4. Tạo thời gian tạo và hết hạn
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15); // Hết hạn sau 15 phút
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // 5. Xử lý Hash và tạo URL (Quan trọng: Phải sắp xếp tham số)
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                try {
                    // Build hash data
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                    // Build query url
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return ""; // Hoặc throw exception
                }
            }
        }

        // 6. Tạo chữ ký bảo mật
        String queryUrl = query.toString();
        String vnp_SecureHash = hmacSHA512(VNP_HASH_SECRET, hashData.toString());

        // 7. URL cuối cùng
        String paymentUrl = VNP_PAY_URL + "?" + queryUrl + "&vnp_SecureHash=" + vnp_SecureHash;

        return paymentUrl;
    }

//    public String createPaymentUrl(String orderGroupId, double totalAmount) {
//        String vnp_TxnRef = orderGroupId;
//        long amount = (long) (totalAmount * 100); // VNPay tính đơn vị đồng * 100
//
//        // Tạo URL giả để test flow trên local
//        return "http://localhost:8080/api/v1/payment/vnpay-return?" +
//                "vnp_TxnRef=" + vnp_TxnRef +
//                "&vnp_ResponseCode=00" + // 00 là thành công
//                "&vnp_Amount=" + amount +
//                "&vnp_OrderInfo=Thanh+toan+nhom+ve";
//    }
}