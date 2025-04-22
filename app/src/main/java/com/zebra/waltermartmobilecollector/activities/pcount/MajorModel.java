package com.zebra.waltermartmobilecollector.activities.pcount;

public class MajorModel {

    private String location;
    private int p1SKU = 0, p2SKU = 0, totalSKU = 0, hashTotal = 0, skuVariance = 0, p1Correct = 0, p2Correct = 0, withdrawal = 0, recon = 0;

    public MajorModel(String location) {
        this.location = location;
    }

    public void addP1SKU(){
        p1SKU++;
    }

    public void addP2SKU(){
        p2SKU++;
    }

    public void addP1Correct(){
        p1Correct++;
    }

    public void addP2Correct(){
        p2Correct++;
    }

    public void addSKUVariance(){
        skuVariance++;
    }

    public void addTotalSKU(){
        totalSKU++;
    }

    public void addHashTotal(String qty){
        try {
            int q = Integer.parseInt(qty);
            hashTotal += q;
        } catch (Exception e){}
    }

    public void setWithdrawal(int withdrawal) {
        this.withdrawal = withdrawal;
    }

    public void recon(){
        recon = 1;
    }

    public String getLocation() {
        return location;
    }

    public int getP1SKU() {
        return p1SKU;
    }

    public int getP2SKU() {
        return p2SKU;
    }

    public int getTotalSKU() {
        return totalSKU;
    }

    public int getHashTotal() {
        return hashTotal;
    }

    public int getSkuVariance() {
        return skuVariance;
    }

    public int getP1Correct() {
        return p1Correct;
    }

    public int getP2Correct() {
        return p2Correct;
    }

    public int getWithdrawal() {
        return withdrawal;
    }

    public int getFinalHash(){
        return (hashTotal - withdrawal);
    }

    public int getRecon() {
        return recon;
    }
    public boolean isRecon() {
        return recon == 1;
    }

}
