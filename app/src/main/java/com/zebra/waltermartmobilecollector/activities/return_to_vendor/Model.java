package com.zebra.waltermartmobilecollector.activities.return_to_vendor;

import com.zebra.waltermartmobilecollector.Helper;

public class Model {

    private String id, upc, sku, desc, qty, vendor, reason, inputed_qty, store, updatedQty;
    private boolean allowed = true, isDecimal = false;

    public Model(String upc, String sku, String desc, String qty) {
        this.upc = upc;
        this.sku = sku;
        this.desc = desc;
        this.qty = qty;
    }

    public Model(String upc, String desc, String vendor, String reason, String inputed_qty, String store) {
        this.upc = upc;
        this.desc = desc;
        this.vendor = vendor;
        this.reason = reason;
        this.inputed_qty = inputed_qty;
        this.store = store;
    }

    public Model(String id, String upc, String sku, String desc, String qty) {
        this.id = id;
        this.upc = upc;
        this.sku = sku;
        this.desc = desc;
        this.qty = qty;
    }

    public Model(String id, String upc, String sku, String desc, String qty, String vendor, String reason, String inputed_qty) {
        this.id = id;
        this.upc = upc;
        this.sku = sku;
        this.desc = desc;
        this.qty = qty;
        this.vendor = vendor;
        this.reason = reason;
        this.inputed_qty = inputed_qty;
    }

    public String getStore() {
        return store;
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

    public String getQty() {
        if (qty == null) return qty;
        return qty.replace(".00", "").replace(".0", "");
    }

    public String getVendor() {
        return vendor;
    }

    public String getReason() {
        return reason;
    }

    public String getInputed_qty() {
        return inputed_qty;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public boolean isDecimal() {
        return isDecimal;
    }

    public void setDecimal() {
        isDecimal = true;
    }

    public void setIsAllowed(String type, String cpotag) {
        allowed = Helper.isOutright(type, cpotag);
    }

    public String getUpdatedQty() {
        return updatedQty;
    }

    public void setUpdatedQty(String updatedQty) {
        this.updatedQty = updatedQty;
    }
}
