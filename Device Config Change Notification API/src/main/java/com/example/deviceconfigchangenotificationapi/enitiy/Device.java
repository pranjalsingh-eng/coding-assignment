package com.example.deviceconfigchangenotificationapi.enitiy;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "devices")
@Data
@NoArgsConstructor
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_IP")
    private String deviceIP;

    @Column(name = "device_details")
    private String deviceDetails;

    @Column(name = "config_changed")
    private Boolean configChanged = false;
}
