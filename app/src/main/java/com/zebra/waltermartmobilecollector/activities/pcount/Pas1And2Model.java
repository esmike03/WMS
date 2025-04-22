package com.zebra.waltermartmobilecollector.activities.pcount;

public class Pas1And2Model {

    private String pas1="0", pas2="", pas3="0", sku;

    public Pas1And2Model(String sku) {
        this.sku = sku;
    }

    public String getSku() {
        return sku;
    }

    public String getPas1() {
        return pas1;
    }

    public void setPas1(String pas1) {
        this.pas1 = pas1;
    }

    public String getPas2() {
        return pas2;
    }

    public void setPas2(String pas2) {
        this.pas2 = pas2;
    }

    public String getPas3() {
        return pas3;
    }

    public void setPas3(String pas3) {
        this.pas3 = pas3;
    }

    public boolean isPas1Correct(){
        return pas1.equals(pas3);
    }

    public boolean isPas2Correct(){
        return pas2.equals(pas3);
    }

}
