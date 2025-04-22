package com.zebra.waltermartmobilecollector.activities.pcount;

public class Model {

    private String id, upc, sku, desc, vendor, updatedQty, location, barcode;

    public Model(String id, String upc, String sku, String desc, String vendor, String updatedQty) {
        this.id = id;
        this.upc = upc;
        this.sku = sku;
        this.desc = desc;
        this.vendor = vendor;
        this.updatedQty = updatedQty;
    }

    public Model(String id, String upc, String sku, String desc, String vendor, String updatedQty, String location) {
        this.id = id;
        this.upc = upc;
        this.sku = sku;
        this.desc = desc;
        this.vendor = vendor;
        this.updatedQty = updatedQty;
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public String getId() {
        return id;
    }

    public String getUpc() {
        return upc;
    }

    public String getSku() {
        return sku;
    }

    public String getDesc() {
        return desc;
    }

    public String getVendor() {
        return vendor;
    }

    public String getUpdatedQty() {
        return updatedQty;
    }

    public void setUpdatedQty(String updatedQty) {
        this.updatedQty = updatedQty;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
}
