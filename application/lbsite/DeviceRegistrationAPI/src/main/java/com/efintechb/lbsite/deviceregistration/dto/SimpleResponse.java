package com.efintechb.lbsite.deviceregistration.dto;

public class SimpleResponse {
    private int statusCode;

    public SimpleResponse() {}

    public SimpleResponse(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
