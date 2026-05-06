package com.example.deviceconfigchangenotificationapi.controller;

import com.example.deviceconfigchangenotificationapi.enitiy.Device;
import com.example.deviceconfigchangenotificationapi.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceRepository deviceRepository;
    private final SseEmitterRegistry emitterRegistry;

    @GetMapping
    public ResponseEntity<List<Device>> getAllDevices() {
        return ResponseEntity.ok(deviceRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Device> getDeviceById(@PathVariable Long id) {
        return deviceRepository.findById(Math.toIntExact(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

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