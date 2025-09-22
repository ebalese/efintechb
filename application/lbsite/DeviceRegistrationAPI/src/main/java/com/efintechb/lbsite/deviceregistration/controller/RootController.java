package com.efintechb.lbsite.deviceregistration.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> body = new HashMap<>();
        body.put("service", "DeviceRegistrationAPI");
        body.put("status", "up");
        return ResponseEntity.ok(body);
    }
}
