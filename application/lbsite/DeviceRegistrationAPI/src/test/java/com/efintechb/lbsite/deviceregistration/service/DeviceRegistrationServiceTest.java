package com.efintechb.lbsite.deviceregistration.service;

import com.efintechb.lbsite.deviceregistration.dto.RegisterRequest;
import com.efintechb.lbsite.deviceregistration.repository.DeviceRegistrationRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class DeviceRegistrationServiceTest {

    private DeviceRegistrationRepository repository;
    private DeviceRegistrationService service;

    @BeforeEach
    void setup() {
        repository = Mockito.mock(DeviceRegistrationRepository.class);
        service = new DeviceRegistrationService(repository);
    }

    @Test
    void register_validRequest_savesEntity() {
        RegisterRequest req = new RegisterRequest();
        req.setUserKey("user-1");
        req.setDeviceType("ios"); // lower case to test normalization

        service.register(req);

        ArgumentCaptor<com.efintechb.lbsite.deviceregistration.model.DeviceRegistration> captor =
                ArgumentCaptor.forClass(com.efintechb.lbsite.deviceregistration.model.DeviceRegistration.class);
        verify(repository, times(1)).save(captor.capture());

        var saved = captor.getValue();
        Assertions.assertEquals("user-1", saved.getUserKey());
        Assertions.assertEquals("IOS", saved.getDeviceType()); // normalized
        Assertions.assertNotNull(saved.getCreatedAt());
    }

    @Test
    void register_invalidDeviceType_throws() {
        RegisterRequest req = new RegisterRequest();
        req.setUserKey("user-1");
        req.setDeviceType("blackberry");

        Assertions.assertThrows(IllegalArgumentException.class, () -> service.register(req));
        verify(repository, never()).save(any());
    }
}
