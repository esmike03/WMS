package com.zebra.waltermartmobilecollector.activities.purchase_order;

import com.zebra.waltermartmobilecollector.Helper;

public class Model {
    private String id, po, sku, desc, updatedQty, barcode, mainID, si;
    private int qty, factor;

    public Model(String po, String sku, String desc, String qty, String factor) {
        this.po = po;
        this.sku = sku;
        this.desc = desc;
        this.qty = Helper.convertToIntAndRemoveDot(qty);
        this.factor = Helper.convertToIntAndRemoveDot(factor);
    }

    public Model(String po,String sku, String desc, String qty) {
        this.po = po;
        this.sku = sku;
        this.desc = desc;
        this.qty = Helper.convertToIntAndRemoveDot(qty);
    }

    public Model(String id, String desc, String qty) {
        this.id = id;
        this.desc = desc;
        this.qty = Helper.convertToIntAndRemoveDot(qty);
    }

    public String getMainID() {
        return mainID;
    }

    public void setMainID(String mainID) {
        this.mainID = mainID;
    }

    public String getId() {
        return id;
    }

    public String getPo() {
        return po;
    }

    public String getSi() {
        return si;
    }
    public void setSi(String si) {
        this.si = si;
    }

    public String getSku() {
        return sku;
    }

    public String getDesc() {
        return desc;
    }

    public int getQty() {
        return qty;
//        return qty.replace(".00", "").replace(".0", "");
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public int getFactor() {
        return factor;
    }

    public void setFactor(String factor) {
        this.factor = Helper.convertToIntAndRemoveDot(factor);
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getUpdatedQty() {
        return updatedQty;
    }

    public void setUpdatedQty(String updatedQty) {
        this.updatedQty = updatedQty;
    }


}


