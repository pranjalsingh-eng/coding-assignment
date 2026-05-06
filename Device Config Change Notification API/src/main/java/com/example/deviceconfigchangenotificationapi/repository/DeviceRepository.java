package com.example.deviceconfigchangenotificationapi.repository;

import com.example.deviceconfigchangenotificationapi.enitiy.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository  extends JpaRepository<Device,Integer> {
    List<Device> findByConfigChangedTrue();
}
