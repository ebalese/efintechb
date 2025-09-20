package com.efintechb.lbsite.deviceregistration.controller;

import com.efintechb.lbsite.deviceregistration.dto.RegisterRequest;
import com.efintechb.lbsite.deviceregistration.dto.SimpleResponse;
import com.efintechb.lbsite.deviceregistration.service.DeviceRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Device")
public class DeviceController {

    private final DeviceRegistrationService service;

    public DeviceController(DeviceRegistrationService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<SimpleResponse> register(@Valid @RequestBody RegisterRequest request) {
        service.register(request);
        return ResponseEntity.ok(new SimpleResponse(200));
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<SimpleResponse> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new SimpleResponse(400));
    }
}
