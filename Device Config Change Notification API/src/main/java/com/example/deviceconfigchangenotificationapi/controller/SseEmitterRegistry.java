package com.example.deviceconfigchangenotificationapi.controller;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SseEmitterRegistry {
    private final List<SseEmitter> emitters = new  CopyOnWriteArrayList<>();
    public SseEmitter register() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        return emitter;
    }

    public void sendToAll(Object data) {
        emitters.forEach(e -> {
            try { e.send(SseEmitter.event().data(data)); }
            catch (Exception ex) { emitters.remove(e); }
        });
    }
}
