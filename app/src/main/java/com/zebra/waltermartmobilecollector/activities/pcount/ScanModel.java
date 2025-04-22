package com.zebra.waltermartmobilecollector.activities.pcount;

public class ScanModel {

    private String sku, desc, barcode, mainID, location, option;
    private int qty;
    private boolean isOutright = true;

    public ScanModel(String sku, String desc, String barcode, String mainID, String qty, String location, String option) {
        this.sku = sku;
        this.desc = desc;
        this.barcode = barcode;
        this.mainID = mainID;
        this.location = location;
        this.option = option;
        try{
            this.qty = Integer.parseInt(qty);
        } catch (Exception e){
            this.qty = 0;
        }
    }

    public ScanModel(String desc, String sku, String barcode, String mainID, String qty, boolean isOutright) {
        this.desc = desc;
        this.sku = sku;
        this.barcode = barcode;
        this.mainID = mainID;
        this.isOutright = isOutright;
        try{
            this.qty = Integer.parseInt(qty);
        } catch (Exception e){
            this.qty = 0;
        }
    }

    public String getSku() {
        return sku;
    }

    public String getDesc() {
        return desc;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getMainID() {
        return mainID;
    }

    public int getQty() {
        return qty;
    }

    public String getLocation() {
        return location;
    }

    public String getOption() {
        return option;
    }

    public boolean isOutright() {
        return isOutright;
    }
}
