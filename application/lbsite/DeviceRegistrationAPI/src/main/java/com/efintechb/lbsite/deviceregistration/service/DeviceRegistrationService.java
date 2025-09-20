package com.efintechb.lbsite.deviceregistration.service;

import com.efintechb.lbsite.deviceregistration.dto.RegisterRequest;
import com.efintechb.lbsite.deviceregistration.model.DeviceRegistration;
import com.efintechb.lbsite.deviceregistration.model.DeviceType;
import com.efintechb.lbsite.deviceregistration.repository.DeviceRegistrationRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class DeviceRegistrationService {

    private final DeviceRegistrationRepository repository;

    public DeviceRegistrationService(DeviceRegistrationRepository repository) {
        this.repository = repository;
    }

    public void register(RegisterRequest request) {
        // basic normalization/validation
        if (!DeviceType.isValid(request.getDeviceType())) {
            throw new IllegalArgumentException("Invalid deviceType. Allowed: IOS, ANDROID, WATCH, TV");
        }
        String normalizedDeviceType = request.getDeviceType().toUpperCase();

        DeviceRegistration dr = new DeviceRegistration();
        dr.setUserKey(request.getUserKey());
        dr.setDeviceType(normalizedDeviceType);
        dr.setCreatedAt(OffsetDateTime.now());
        repository.save(dr);
    }
}
