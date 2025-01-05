package com.kailasnath.locator;

public class BusLocationAndRecordStatus {

    private int busId;
    private double latitude;
    private double longitude;
    private boolean record;

    public BusLocationAndRecordStatus(int busId, double latitude, double longitude, boolean record) {
        this.busId = busId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.record = record;
    }
}
