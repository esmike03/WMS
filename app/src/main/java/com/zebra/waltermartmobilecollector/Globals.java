package com.zebra.waltermartmobilecollector;

import android.database.sqlite.SQLiteDatabase;

import com.zebra.waltermartmobilecollector.activities.user.Model;
import com.zebra.waltermartmobilecollector.services.SettingsService;

public final class Globals {

    public static SQLiteDatabase db;

    public static String name, username, userId, ftpStoreCode, masterfileUpdatedAt="", selectedLocation, stockCountOption = "1";

    public static Model selectedUser;
    public static boolean singleValidation = false, reportMatch = true;

    private static String storeCode, ipAddress, ftpUser, ftpPassword, userRole;

    public static String poMode = null; // "MP2" or "MPO"

    private static boolean isWMS = true;

    public static boolean userIsAdmin() {
        return userRole.equals("Admin");
    }
    public static boolean userIsSuperAdmin() {
        return userRole.equals("Super Admin");
    }
    public static boolean userIsNormalUser() {
        return userRole.equals("User");
    }
    public static void setUserRole(String role) {
        userRole = role;
    }

    public static String getLocalStoreCode() {
        if (storeCode == null)
            SettingsService.fetchSettings();

        return storeCode;
    }

    public static String getStoreCode() {
        if (ftpStoreCode != null) return ftpStoreCode;
        if (storeCode == null)
            SettingsService.fetchSettings();

        return storeCode;
    }

    public static String getIpAddress() {
        if (ipAddress == null)
            SettingsService.fetchSettings();

        return ipAddress;
    }

    public static String getFtpUser() {
        if (ipAddress == null)
            SettingsService.fetchSettings();

        return ftpUser;
    }

    public static String getFtpPassword() {
        if (ipAddress == null)
            SettingsService.fetchSettings();

        return ftpPassword;
    }

    public static boolean isWMS() {
        if (ipAddress == null)
            SettingsService.fetchSettings();

        return isWMS;
    }

    public static void setSettings(boolean wms, String sCode, String ip, String user, String password){
        isWMS = wms;
        storeCode = sCode;
        ipAddress = ip;
        ftpUser = user;
        ftpPassword = password;
    }

    public static void resetSettings(){
        storeCode = null;
        ipAddress = null;
        ftpUser = null;
        ftpPassword = null;
        isWMS = true;
    }

}
