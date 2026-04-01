package com.zebra.waltermartmobilecollector.activities.purchase_order.auto_matching;

import com.zebra.waltermartmobilecollector.Helper;

public class Model {

    private String id, sku, barcode, desc;
    private int pas1 = 0, pas2 = 0, pas3 = 0, factor = 0, pcs = 0;

    public Model(String id, String sku, String barcode, String desc, String pcs, String factor) {
        this.id = id;
        this.sku = sku;
        this.barcode = barcode;
        this.desc = desc;
        setPcs(pcs);
        setFactor(factor);
    }

    public Model(String id, String sku, String barcode, String desc) {
        this.id = id;
        this.sku = sku;
        this.barcode = barcode;
        this.desc = desc;
    }

    public Model(String id, String sku, String barcode, String desc, String pas3) {
        this.id = id;
        this.sku = sku;
        this.barcode = barcode;
        this.desc = desc;
        if (pas3 == null) return;
        setPas3(pas3);
    }

    public Model(String id, String sku, String barcode, String desc, String pcs, String factor, String pas3) {
        this.id = id;
        this.sku = sku;
        this.barcode = barcode;
        this.desc = desc;
        setPcs(pcs);
        setFactor(factor);
        if (pas3 == null) return;
        setPas3(pas3);
        this.pas3 = this.pas3 * this.factor;
    }

    public Model (String sku, String barcode, String desc, String p1, String p2, String p3, boolean none){
        this.sku = sku;
        this.barcode = barcode;
        this.desc = desc;
        setPas1(p1);
        setPas2(p2);
        setPas3(p3);
    }

    public void setPcsAndFactor(String p, String f){
        setPcs(p);
        setFactor(f);
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setFactor(String factor) {
        this.factor = Helper.convertToIntAndRemoveDot(factor);
    }

    public void setPcs(String pcs) {
        this.pcs = Helper.convertToIntAndRemoveDot(pcs);
    }

    public String getBarcode() {
        return barcode;
    }

    public String getDesc() {
        return desc;
    }

    public void setPas1(String pas1) {
        if (!pas1.isEmpty())
            this.pas1 = Integer.parseInt(pas1);
        else
            this.pas1 = 0;
    }

    public void setPas2(String pas2) {
        if (!pas2.isEmpty())
            this.pas2 = Integer.parseInt(pas2);
        else
            this.pas2 = 0;
    }

    public void setPas3(String pas3) {
        if (!pas3.isEmpty())
            this.pas3 = Integer.parseInt(pas3);
        else
            this.pas3 = 0;
    }

    public void setPas3(int pas3) {
        this.pas3 = pas3;
    }

    public String getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public int getPas1() {
        return pas1;
    }

    public int getPas2() {
        return pas2;
    }

    public int getPas3() {
        return pas3;
    }

    public int getFactor() {
        return factor;
    }

    public int getPcs() {
        return pcs;
    }

    private String pas1Username, pas2Username, pas1Date, pas2Date;

    public String getPas1Username() { return pas1Username; }
    public void setPas1Username(String pas1Username) { this.pas1Username = pas1Username; }

    public String getPas2Username() { return pas2Username; }
    public void setPas2Username(String pas2Username) { this.pas2Username = pas2Username; }

    public String getPas1Date() { return pas1Date; }
    public void setPas1Date(String pas1Date) { this.pas1Date = pas1Date; }

    public String getPas2Date() { return pas2Date; }
    public void setPas2Date(String pas2Date) { this.pas2Date = pas2Date; }
}
