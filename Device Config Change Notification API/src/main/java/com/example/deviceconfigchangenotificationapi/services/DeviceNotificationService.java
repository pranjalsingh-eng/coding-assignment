package com.example.deviceconfigchangenotificationapi.services;

import com.example.deviceconfigchangenotificationapi.controller.SseEmitterRegistry;
import com.example.deviceconfigchangenotificationapi.dto.DeviceNotificationDTO;
import com.example.deviceconfigchangenotificationapi.enitiy.Device;
import com.example.deviceconfigchangenotificationapi.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceNotificationService {
    private final DeviceRepository deviceRepository;
    private  final SseEmitterRegistry emitterRegistry;

    @Scheduled(fixedDelay = 5000)
    public void deviceConfigNotification() {
        List<Device> changeDevices = deviceRepository.findByConfigChangedTrue();
        for (Device device : changeDevices) {
            DeviceNotificationDTO notificationDTO = new DeviceNotificationDTO(
                    device.getId(),
                    device.getDeviceIP(),
                    "Configuration changed for device: " + device.getDeviceIP(),
                    LocalDateTime.now()
            );

            emitterRegistry.sendToAll(notificationDTO); // push JSON to all SSE clients
            log.info("Notification sent for device: {}", device.getId());
            device.setConfigChanged(false); // reset flag
            deviceRepository.save(device);
        }
    }
}
