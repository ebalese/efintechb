package com.efintechb.lbsite.statistics.dto;

public class StatisticsResponse {
    private String deviceType;
    private long count;

    public StatisticsResponse() {}

    public StatisticsResponse(String deviceType, long count) {
        this.deviceType = deviceType;
        this.count = count;
    }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
}
