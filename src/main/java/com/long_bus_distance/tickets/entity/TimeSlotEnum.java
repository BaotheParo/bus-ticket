// TimeSlotEnum.java
package com.long_bus_distance.tickets.entity;

import java.time.LocalTime;

public enum TimeSlotEnum {
    EARLY_MORNING(LocalTime.of(0, 0), LocalTime.of(6, 0)), // Sáng sớm
    MORNING(LocalTime.of(6, 0), LocalTime.of(12, 0)), // Buổi sáng
    AFTERNOON(LocalTime.of(12, 0), LocalTime.of(18, 0)), // Buổi chiều
    EVENING(LocalTime.of(18, 0), LocalTime.of(23, 59)); // Buổi tối

    private final LocalTime startTime;
    private final LocalTime endTime;

    TimeSlotEnum(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
}