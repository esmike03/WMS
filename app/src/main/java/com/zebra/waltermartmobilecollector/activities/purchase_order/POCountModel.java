package com.zebra.waltermartmobilecollector.activities.purchase_order;

public class POCountModel {

    private String po, count;

    public POCountModel(String po, String count) {
        this.po = po;
        this.count = count;
    }

    public String getPo() {
        return po;
    }

    public String getCount() {
        return count;
    }
}
