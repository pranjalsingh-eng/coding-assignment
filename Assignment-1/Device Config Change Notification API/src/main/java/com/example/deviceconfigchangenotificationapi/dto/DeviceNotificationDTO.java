package com.example.deviceconfigchangenotificationapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DeviceNotificationDTO {

    private Long deviceId;
    private String deviceIP;
    private String deviceDetails;
    private String message;
    private LocalDateTime timestamp;

    public DeviceNotificationDTO(Long id, String deviceIP, String deviceDetails, LocalDateTime now) {
    }
}
