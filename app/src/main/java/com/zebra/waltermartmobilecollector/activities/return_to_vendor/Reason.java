package com.zebra.waltermartmobilecollector.activities.return_to_vendor;

public class Reason {

    private String code, desc;

    public Reason(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
