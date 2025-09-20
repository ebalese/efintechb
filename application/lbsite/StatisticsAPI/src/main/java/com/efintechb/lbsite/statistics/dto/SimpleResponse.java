package com.efintechb.lbsite.statistics.dto;

public class SimpleResponse {
    private int statusCode;
    private String message;

    public SimpleResponse() {}

    public SimpleResponse(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
