package com.zebra.waltermartmobilecollector.activities.purchase_order;

public class FTPFile {

    private String po, sku,qty;

    public FTPFile(String po, String sku, String qty) {
        this.po = po;
        this.sku = sku;
        this.qty = qty;
    }

    public String getSku() {
        return sku;
    }

    public String getPo() {
        return po;
    }

    public String getQty() {
        return qty;
    }
}
