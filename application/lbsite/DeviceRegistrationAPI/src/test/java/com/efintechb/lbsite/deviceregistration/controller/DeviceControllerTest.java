package com.efintechb.lbsite.deviceregistration.controller;

import com.efintechb.lbsite.deviceregistration.dto.RegisterRequest;
import com.efintechb.lbsite.deviceregistration.service.DeviceRegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceController.class)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeviceRegistrationService service;

    @Test
    void register_returns200OnSuccess() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUserKey("user-1");
        req.setDeviceType("IOS");

        mockMvc.perform(post("/Device/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200));

        Mockito.verify(service).register(Mockito.any());
    }

    @Test
    void register_returns400OnInvalid() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUserKey("user-1");
        req.setDeviceType("BLACKBERRY");

        doThrow(new IllegalArgumentException("invalid")).when(service).register(Mockito.any());

        mockMvc.perform(post("/Device/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }
}
