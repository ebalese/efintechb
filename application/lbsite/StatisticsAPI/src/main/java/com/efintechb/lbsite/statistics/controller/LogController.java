package com.efintechb.lbsite.statistics.controller;

import com.efintechb.lbsite.statistics.dto.AuthLogRequest;
import com.efintechb.lbsite.statistics.dto.SimpleResponse;
import com.efintechb.lbsite.statistics.dto.StatisticsResponse;
import com.efintechb.lbsite.statistics.service.StatisticsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/Log/auth")
public class LogController {

    private final StatisticsService service;

    public LogController(StatisticsService service) {
        this.service = service;
    }

    // POST /Log/auth
    @PostMapping
    public ResponseEntity<SimpleResponse> logAuth(@Valid @RequestBody AuthLogRequest request) {
        SimpleResponse res = service.logAuth(request);
        HttpStatus status = res.getStatusCode() == 200 ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(res);
    }

    // GET /Log/auth/statistics?deviceType=IOS
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics(@RequestParam("deviceType") String deviceType) {
        long count = service.countByDeviceType(deviceType);
        if (count < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new StatisticsResponse(deviceType, -1));
        }
        return ResponseEntity.ok(new StatisticsResponse(deviceType, count));
    }
}
