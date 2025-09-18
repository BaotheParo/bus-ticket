package com.long_bus_distance.tickets.entity;

public enum TripStatusEnum {
    DRAFT, // Khi mà nhà điều hành tạo chuyến đi mới nhưng chưa sẵn sàng để phát hành
    PUBLISHED, //Chuyến đi đã được phát hành và hành khách thấy được
    CANCELLED, //Chuyến đi đã bị hủy và không còn được phát hành
    COMPLETED, //Chuyến đi đã diễn ra và đã đi đến điểm cuối
}
