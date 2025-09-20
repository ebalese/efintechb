package com.efintechb.lbsite.statistics.controller;

import com.efintechb.lbsite.statistics.dto.AuthLogRequest;
import com.efintechb.lbsite.statistics.dto.SimpleResponse;
import com.efintechb.lbsite.statistics.dto.StatisticsResponse;
import com.efintechb.lbsite.statistics.service.StatisticsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LogController.class)
class LogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StatisticsService service;

    @Test
    void postLogAuth_returns200OnSuccess() throws Exception {
        when(service.logAuth(any(AuthLogRequest.class))).thenReturn(new SimpleResponse(200, "success"));

        AuthLogRequest req = new AuthLogRequest();
        req.setUserKey("user-1");
        req.setDeviceType("IOS");

        mockMvc.perform(post("/Log/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        Mockito.verify(service).logAuth(any(AuthLogRequest.class));
    }

    @Test
    void getStatistics_returnsCount() throws Exception {
        when(service.countByDeviceType("IOS")).thenReturn(5L);

        mockMvc.perform(get("/Log/auth/statistics").param("deviceType", "IOS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceType").value("IOS"))
                .andExpect(jsonPath("$.count").value(5));
    }

    @Test
    void getStatistics_invalidDevice_returnsBadRequestWithMinusOne() throws Exception {
        when(service.countByDeviceType("BLACKBERRY")).thenReturn(-1L);

        mockMvc.perform(get("/Log/auth/statistics").param("deviceType", "BLACKBERRY"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.deviceType").value("BLACKBERRY"))
                .andExpect(jsonPath("$.count").value(-1));
    }
}
