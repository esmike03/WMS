package com.zebra.waltermartmobilecollector.activities.pcount;

public class LocationReportModel {

    private String location;
    private int qty;

    public LocationReportModel(String location, String qty) {
        this.location = location;
        try {
            this.qty = Integer.parseInt(qty);
        } catch (Exception e) {
            this.qty = 0;
        }
    }

    public String getLocation() {
        return location;
    }

    public int getQty() {
        return qty;
    }

    public void add(String q) {
        try {
            int n = Integer.parseInt(q);
            qty += n;
        } catch (Exception e) {
        }
    }

}
