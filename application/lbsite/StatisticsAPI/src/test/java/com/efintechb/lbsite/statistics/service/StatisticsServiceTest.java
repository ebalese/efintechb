package com.efintechb.lbsite.statistics.service;

import com.efintechb.lbsite.statistics.client.DeviceRegistrationClient;
import com.efintechb.lbsite.statistics.dto.AuthLogRequest;
import com.efintechb.lbsite.statistics.dto.SimpleResponse;
import com.efintechb.lbsite.statistics.repository.DeviceRegistrationRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class StatisticsServiceTest {

    private DeviceRegistrationRepository repository;
    private DeviceRegistrationClient client;
    private StatisticsService service;

    @BeforeEach
    void setup() {
        repository = Mockito.mock(DeviceRegistrationRepository.class);
        client = Mockito.mock(DeviceRegistrationClient.class);
        service = new StatisticsService(repository, client);
    }

    @Test
    void logAuth_validDeviceType_mapsSuccessMessage() {
        AuthLogRequest req = new AuthLogRequest();
        req.setUserKey("user-1");
        req.setDeviceType("ios");
        when(client.register(any())).thenReturn(new SimpleResponse(200, null));

        var resp = service.logAuth(req);

        Assertions.assertEquals(200, resp.getStatusCode());
        Assertions.assertEquals("success", resp.getMessage());
    }

    @Test
    void logAuth_invalidDeviceType_returnsBadRequest() {
        AuthLogRequest req = new AuthLogRequest();
        req.setUserKey("user-1");
        req.setDeviceType("blackberry");

        var resp = service.logAuth(req);

        Assertions.assertEquals(400, resp.getStatusCode());
        Assertions.assertEquals("bad_request", resp.getMessage());
    }

    @Test
    void countByDeviceType_valid_usesRepository() {
        when(repository.countByDeviceTypeIgnoreCase("IOS")).thenReturn(5L);
        long count = service.countByDeviceType("ios");
        Assertions.assertEquals(5L, count);
    }

    @Test
    void countByDeviceType_invalid_returnsMinusOne() {
        long count = service.countByDeviceType("blackberry");
        Assertions.assertEquals(-1L, count);
    }
}
