package com.zebra.waltermartmobilecollector.activities.purchase_order;

public class AMModel {

    private String report, finalTxt, skuReport,receipt;
    private int totalBoxExpected = 0, totalPcsExpected = 0;
    private boolean matched = true;

    public boolean isMatched() {
        return matched;
    }

    public void unmatch() {
        this.matched = false;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public String getFinalTxt() {
        return finalTxt;
    }

    public void setFinalTxt(String finalTxt) {
        this.finalTxt = finalTxt;
    }

    public String getSkuReport() {
        return skuReport;
    }

    public void setSkuReport(String skuReport) {
        this.skuReport = skuReport;
    }

    public int getTotalBoxExpected() {
        return totalBoxExpected;
    }

    public void setTotalBoxExpected(int totalBoxExpected) {
        this.totalBoxExpected = totalBoxExpected;
    }

    public int getTotalPcsExpected() {
        return totalPcsExpected;
    }

    public void setTotalPcsExpected(int totalPcsExpected) {
        this.totalPcsExpected = totalPcsExpected;
    }

    public String getReceipt() {
        return receipt;
    }

    public void setReceipt(String receipt) {
        this.receipt = receipt;
    }
}
