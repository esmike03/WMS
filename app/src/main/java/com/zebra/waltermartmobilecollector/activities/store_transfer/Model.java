package com.zebra.waltermartmobilecollector.activities.store_transfer;

public class Model {

    private String id, upc, sku, desc, qty, fromLoc, toLoc;
    private boolean isPerPiece = true;



    public Model(String id, String upc, String sku, String desc, String qty) {
        this.id = id;
        this.upc = upc;
        this.sku = sku;
        this.desc = desc;
        this.qty = qty;
    }

    public Model(String id, String sku, String desc, String qty, boolean isPerPiece, String fromLoc, String toLoc) {
        this.id = id;
        this.sku = sku;
        this.desc = desc;
        this.qty = qty;
        this.fromLoc = fromLoc;
        this.toLoc = toLoc;
        this.isPerPiece = isPerPiece;
    }

    public Model(String id, String upc, String sku, String qty, String fromLoc, String toLoc, boolean isPerPiece) {
        this.id = id;
        this.upc = upc;
        this.sku = sku;
        this.qty = qty;
        this.fromLoc = fromLoc;
        this.toLoc = toLoc;
        this.isPerPiece = isPerPiece;
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
        return qty;
    }

    public String getFromLoc() {
        return fromLoc;
    }

    public String getToLoc() {
        return toLoc;
    }

    public boolean isPerPiece() {
        return isPerPiece;
    }

    public void setPerPiece(boolean perPiece) {
        isPerPiece = perPiece;
    }
}
