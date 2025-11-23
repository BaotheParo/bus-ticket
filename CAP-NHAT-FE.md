
# 📄 Báo Cáo Yêu Cầu Cập Nhật Frontend (Integration Phase)

-----

## 1\. Tổng quan thay đổi

Hệ thống Backend đã cập nhật quy trình đặt vé mới:

1.  **Booking Flow:** Không còn trả về thông tin vé thành công ngay lập tức. API sẽ trả về **URL thanh toán VNPay**.
2.  **Concurrency:** Nếu 2 người cùng chọn 1 ghế, người chậm hơn sẽ nhận lỗi 400 (Bad Request) thay vì 500.
3.  **Payment Status:** Vé có thêm trạng thái `PENDING_PAYMENT` (Chờ thanh toán) và `FAILED` (Thất bại).

-----

## 2\. Chi tiết yêu cầu cập nhật

### 2.1. Trang Mua Vé (`src/app/(customer)/mua-ve/page.tsx`)

Hiện tại trang này đang dùng `MOCK_DATA` và xử lý giả lập (`setView("SUCCESS")`). Cần chuyển sang gọi API thật.

**Yêu cầu thay đổi:**

* **Logic nút "Thanh toán":**
    * **Cũ:** `setView("SUCCESS")` (Hiển thị UI thành công giả).
    * **Mới:** Gọi API `POST /api/v1/tickets`.
    * **Payload:**
      ```json
      {
          "tripId": "uuid-cua-chuyen-xe",
          "deckId": "uuid-cua-tang",
          "selectedSeat": "A1" // Số ghế người dùng chọn
      }
      ```
    * **Xử lý phản hồi thành công (200 OK):** Backend sẽ trả về một chuỗi URL (VNPay Sandbox URL). Frontend cần thực hiện chuyển hướng người dùng:
      ```javascript
      window.location.href = response.data; // Redirect sang VNPay
      ```
    * **Xử lý lỗi (400 Bad Request - Redis Lock):**
        * Khi Backend trả về lỗi với message chứa từ khóa "đã được đặt" hoặc "thao tác", cần hiển thị **Toast Error** hoặc **Alert**: *"Ghế này vừa có người khác chọn. Vui lòng chọn ghế khác\!"*.
        * Đồng thời tự động bỏ chọn ghế đó trên giao diện (cập nhật state `selectedSeats`).

**Snippet gợi ý cho `handleConfirmPayment`:**

```typescript
const handleConfirmPayment = async () => {
  try {
    // Gọi API Backend
    const response = await api.post('/api/v1/tickets', {
       tripId: tripData.tripId,
       deckId: currentDeckId, // Cần lấy ID tầng từ data
       selectedSeat: selectedSeats[0].replace(/[A-Z]/, '') // Chỉ lấy số nếu BE yêu cầu số, hoặc giữ nguyên nếu BE cần "A1"
    });

    // Redirect sang VNPay
    if (response.data) {
       window.location.href = response.data;
    }
  } catch (error: any) {
    if (error.response?.status === 400) {
       alert("Ghế này vừa bị người khác nhanh tay đặt mất rồi! 😢");
       // Logic reload lại sơ đồ ghế
    } else {
       alert("Lỗi hệ thống, vui lòng thử lại.");
    }
  }
};
```

### 2.2. Tạo trang Kết quả Thanh toán (`New Page`)

Sau khi thanh toán ở VNPay, người dùng sẽ bị redirect về lại Web. Cần tạo một trang mới để hứng kết quả này.

* **Đường dẫn đề xuất:** `src/app/(customer)/payment-result/page.tsx`
* **Nhiệm vụ:** Đọc các tham số từ URL (`vnp_ResponseCode`, `vnp_TxnRef`).
* **Logic hiển thị:**
    * Nếu `vnp_ResponseCode === "00"`: Hiển thị **Card Thành công** (Giống cái UI `view === "SUCCESS"` đang có ở trang mua vé). Kèm nút "Xem vé của tôi".
    * Nếu `vnp_ResponseCode !== "00"`: Hiển thị **Card Thất bại** (Màu đỏ, icon X). Kèm thông báo "Giao dịch bị hủy hoặc lỗi". Nút "Thử lại" quay về trang chủ.

### 2.3. Trang Lịch sử mua vé (`src/app/(customer)/profile/lich-su-mua-ve/page.tsx`)

Cần cập nhật `BookingTable` để hiển thị đúng các trạng thái mới từ Backend.

**Yêu cầu cập nhật UI:**

* **Mapping Trạng thái (Payment Status):**
    * `paid` -\> Badge Xanh lá ("Đã thanh toán").
    * `pending` -\> Badge Vàng ("Chờ thanh toán / Giữ chỗ").
    * `failed` -\> Badge Đỏ ("Thất bại").
* **Logic hành động:**
    * Nếu trạng thái là `pending`: Hiển thị nút **"Thanh toán ngay"** (Gọi lại API lấy link VNPay hoặc mở lại link cũ).

### 2.4. Trang Tìm kiếm (`src/components/ticket-search-card.tsx`)

Phần này Backend đã tích hợp **Redis Cache**. Frontend không cần sửa code nhưng cần lưu ý về UX.

* **Lưu ý UX:** Lần tìm kiếm đầu tiên có thể mất \~200ms, nhưng các lần sau (khi filter, sort lại cùng tiêu chí) sẽ cực nhanh (\<50ms).
* **Parameter:** Đảm bảo gửi đúng định dạng ngày tháng `YYYY-MM-DD` lên API `GET /api/v1/published-trips`. Hiện tại code đang dùng `date-fns` format, cần kiểm tra kỹ output.

-----

## 3\. Cấu trúc Dữ liệu API (API Contract)

Frontend cần map lại các Interface trong `mock-data.ts` để khớp với phản hồi thực tế của Backend.

**Endpoint: GET /api/v1/published-trips**

```typescript
// Interface cập nhật cho Trip
interface Trip {
  id: string;
  routeName: string;      // Backend: routeName
  departureTime: string;  // ISO String
  departurePoint: string;
  arrivalTime: string;    // ISO String
  destination: string;
  totalAvailableSeats: number; // Backend trả về số int
  // ... các trường khác
}
```

**Endpoint: POST /api/v1/tickets**

* **Request:**
  ```typescript
  {
    tripId: string;
    deckId: string;
    selectedSeat: string; // Ví dụ: "5" (nếu chỉ gửi số) hoặc "A5" (tùy logic BE)
  }
  ```
* **Response (Success):**
  ```typescript
  // Trả về String text (URL)
  "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?..."
  ```

-----

## 4\. Checklist triển khai

1.  [ ]  Tạo biến môi trường `NEXT_PUBLIC_API_URL` trỏ về Backend (`http://localhost:8080`).
2.  [ ]  Cập nhật `axios` instance hoặc fetcher để gọi API thật thay vì Mock data.
3.  [ ]  Sửa `src/app/(customer)/mua-ve/page.tsx`: Thay logic `setView` bằng logic gọi API + Redirect.
4.  [ ]  Tạo trang `src/app/(customer)/payment-result/page.tsx` để xử lý callback từ VNPay.
5.  [ ]  Kiểm tra hiển thị trạng thái `PENDING` và `FAILED` trong trang Lịch sử vé.
6.  [ ]  Test case: Mở 2 tab ẩn danh, cùng chọn 1 ghế để kiểm tra thông báo lỗi Redis Lock.
