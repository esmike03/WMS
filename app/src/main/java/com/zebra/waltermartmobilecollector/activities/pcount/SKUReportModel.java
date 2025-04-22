package com.zebra.waltermartmobilecollector.activities.pcount;

public class SKUReportModel {

    private String barcode, sku, desc;
    private int qty;

    public SKUReportModel(String barcode, String sku, String desc, String qty) {
        this.barcode = barcode;
        this.sku = sku;
        this.desc = desc;
        try {
            this.qty = Integer.parseInt(qty);
        } catch (Exception e) {
            this.qty = 0;
        }
    }

    public SKUReportModel(String barcode, String sku, String desc) {
        this.barcode = barcode;
        this.sku = sku;
        this.desc = desc;
        this.qty = 0;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getSku() {
        return sku;
    }

    public String getDesc() {
        return desc;
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
