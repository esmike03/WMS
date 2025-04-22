package com.zebra.waltermartmobilecollector.activities.syncfile;

public class Model {

    private String filename, folder, newFilename;
    private boolean existing = false;

    public Model(String filename, String folder) {
        this.filename = filename;
        this.folder = folder;
    }

    public String getFilename() {
        return filename;
    }

    public String getFolder() {
        return folder;
    }

    public String getNewFilename() {
        return newFilename == null ? filename : newFilename;
    }

    public void setNewFilename(String nfilename) {
        this.newFilename = nfilename;
    }

    public boolean isExisting() {
        return existing;
    }

    public void setExisting() {
        this.existing = true;
    }
}
