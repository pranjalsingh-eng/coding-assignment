package com.example.deviceconfigchangenotificationapi.controller;

import com.example.deviceconfigchangenotificationapi.enitiy.Device;
import com.example.deviceconfigchangenotificationapi.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceRepository deviceRepository;
    private final SseEmitterRegistry emitterRegistry;

    @GetMapping("/notifications")
    public SseEmitter subscribe() {
        return emitterRegistry.register();
    }

    @PostMapping("/{id}/config-changed")
    public ResponseEntity<String> markConfigChanged(@PathVariable Long id) {
        deviceRepository.findById(Math.toIntExact(id)).ifPresent(d -> {
            d.setConfigChanged(true);
            deviceRepository.save(d);
        });

        return ResponseEntity.ok("Flag set");
    }


}
