package com.zebra.waltermartmobilecollector.activities.purchase_order.auto_matching;

public class POFilename {

    private String po, pas1, pas2;

    public POFilename(String po, String pas1) {
        this.po = po;
        this.pas1 = pas1;
    }

    public POFilename(String po) {
        this.po = po;
    }

    public String getPo() {
        return po;
    }

    public String getPas1() {
        return pas1;
    }

    public String getPas2() {
        return pas2;
    }

    public void setPas1(String pas1) {
        this.pas1 = pas1;
    }

    public void setPas2(String pas2) {
        this.pas2 = pas2;
    }
}
