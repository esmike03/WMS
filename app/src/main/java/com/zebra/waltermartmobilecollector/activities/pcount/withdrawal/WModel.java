package com.zebra.waltermartmobilecollector.activities.pcount.withdrawal;

public class WModel {

    private String barcode;
    private int qty;

    public WModel(String barcode, String qty) {
        this.barcode = barcode;
        try {
            this.qty = Integer.parseInt(qty);
        } catch (Exception e){
            this.qty = 0;
        }
    }

    public String getBarcode() {
        return barcode;
    }

    public int getQty() {
        return qty;
    }

    public void minus(int v){
        qty -= v;
    }
}
