package com.efintechb.lbsite.statistics.service;

import com.efintechb.lbsite.statistics.client.DeviceRegistrationClient;
import com.efintechb.lbsite.statistics.dto.AuthLogRequest;
import com.efintechb.lbsite.statistics.dto.SimpleResponse;
import com.efintechb.lbsite.statistics.model.DeviceType;
import com.efintechb.lbsite.statistics.repository.DeviceRegistrationRepository;
import org.springframework.stereotype.Service;

@Service
public class StatisticsService {

    private final DeviceRegistrationRepository repository;
    private final DeviceRegistrationClient client;

    public StatisticsService(DeviceRegistrationRepository repository, DeviceRegistrationClient client) {
        this.repository = repository;
        this.client = client;
    }

    public SimpleResponse logAuth(AuthLogRequest request) {
        if (!DeviceType.isValid(request.getDeviceType())) {
            return new SimpleResponse(400, "bad_request");
        }
        // optional normalization
        request.setDeviceType(request.getDeviceType().toUpperCase());
        SimpleResponse reg = client.register(request);
        int code = reg.getStatusCode();
        String message = code == 200 ? "success" : "bad_request";
        return new SimpleResponse(code, message);
    }

    public long countByDeviceType(String deviceType) {
        if (!DeviceType.isValid(deviceType)) {
            return -1;
        }
        return repository.countByDeviceTypeIgnoreCase(deviceType);
    }
}
