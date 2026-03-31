package com.zebra.waltermartmobilecollector.activities.price_check;

public class Model {

    private String barcode, desc, sku, rPrice, pPrice, vendorCode, type;
//    private boolean outright;

    public Model(String barcode, String desc, String sku, String rPrice, String pPrice, String vendorCode, String type) {
        this.barcode = barcode;
        this.desc = desc;
        this.sku = sku;
        this.rPrice = rPrice;
        this.pPrice = pPrice;
        this.vendorCode = vendorCode;
        this.type = type;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getDesc() {
        return desc;
    }

    public String getSku() {
        return sku;
    }

    public String getrPrice() {
        return rPrice;
    }

    public String getpPrice() {
        return pPrice;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public String getType() {
        return type;
    }

    public boolean isOutright() {
        return type.equals("Outright");
//        return outright;
    }
}
