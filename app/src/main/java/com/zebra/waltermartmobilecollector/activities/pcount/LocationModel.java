package com.zebra.waltermartmobilecollector.activities.pcount;

public class LocationModel {

    private String location;
    private boolean processed = false, scanned = false;

    public LocationModel(String location) {
        this.location = location;
    }

    public LocationModel(String location, boolean processed, boolean scanned) {
        this.location = location;
        this.processed = processed;
        this.scanned = scanned;
    }

    public String getLocation() {
        return location;
    }

    public boolean isProcessed() {
        return processed;
    }

    public boolean isScanned() {
        return scanned;
    }
}
