package com.efintechb.lbsite.statistics.client;

import com.efintechb.lbsite.statistics.dto.AuthLogRequest;
import com.efintechb.lbsite.statistics.dto.SimpleResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class DeviceRegistrationClient {

    private final RestTemplate restTemplate;

    @Value("${device.registration.url:http://localhost:8081}")
    private String deviceRegistrationUrl;

    public DeviceRegistrationClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public SimpleResponse register(AuthLogRequest request) {
        String url = deviceRegistrationUrl + "/Device/register";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthLogRequest> entity = new HttpEntity<>(request, headers);
        try {
            ResponseEntity<SimpleResponse> response = restTemplate.postForEntity(url, entity, SimpleResponse.class);
            // DeviceRegistrationAPI returns only { statusCode }
            if (response.getBody() != null) {
                return new SimpleResponse(response.getBody().getStatusCode(), null);
            }
            // Fallback based on HTTP status
            int code = response.getStatusCode().is2xxSuccessful() ? 200 : 400;
            return new SimpleResponse(code, null);
        } catch (RestClientException ex) {
            return new SimpleResponse(400, null);
        }
    }
}
