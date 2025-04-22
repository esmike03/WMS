package com.zebra.waltermartmobilecollector.activities.stock_count;

public class RVModel {

    private String location;
    private int skuCount, total, position = 0;
    private boolean show = false;
//    private ArrayList<Model> data;
    private Model model;

    public RVModel(String location, int skuCount, int total) {
        this.location = location;
        this.skuCount = skuCount;
        this.total = total;
    }

    public String getLocation() {
        return location;
    }

    public int getSkuCount() {
        return skuCount;
    }

    public int getTotal() {
        return total;
    }

    public boolean isShow() {
        return show;
    }

    public void toggleShow() {
        show = !show;
    }
    public void closeShow() {
        show = false;
    }

    public boolean allowedNext(){
        return ((skuCount - 1) > position);
    }

    public boolean allowedPrev(){
        return position > 0;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public int getPosition() {
        return position;
    }

    public void resetPosition() {
        this.position = 0;
    }

    public void addPosition() {
        this.position++;
    }

    public void minusPosition() {
        this.position--;
    }

    //    public ArrayList<Model> getData() {
//        return data;
//    }
//
//    public void setData(ArrayList<Model> data) {
//        this.data = data;
//    }
}
