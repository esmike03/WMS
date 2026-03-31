package com.zebra.waltermartmobilecollector;

public final class Folders {

    public final static String FTP_MASTER_FOLDER = "/AIOS/";

    public final static String MASTERFILES = "/MC_Masterfile/";

    public final static String ARCHIVE = "/Archive/";

    public final static String PO = "/RCR/";
    public final static String SCANNED_PO = FTP_MASTER_FOLDER + "RCR/";
    public final static String UNMATCHED_PO = SCANNED_PO + "Unmatched/";
    public final static String MATCHED_PO = SCANNED_PO + "Matched/";

    public final static String RTV = "/RTV/";
    public final static String SCANNED_RTV = FTP_MASTER_FOLDER + "RTV/";

    public final static String STORE_TRANSFER = "/TRFOUT/";
    public final static String SCANNED_STORE_TRANSFER = FTP_MASTER_FOLDER + "TRFOUT/";

    public final static String PCOUNT = "/PCOUNT/";
    public final static String SCANNED_PCOUNT = FTP_MASTER_FOLDER + "PCOUNT/";
    public final static String MATCHED_PCOUNT = SCANNED_PCOUNT + "Matched/";
    public final static String UNMATCHED_PCOUNT = SCANNED_PCOUNT+ "Unmatched/";
    public final static String WITHDRAWAL_PCOUNT = SCANNED_PCOUNT+ "Withdrawal/";
    public final static String REPORTS_PCOUNT = SCANNED_PCOUNT+ "Reports/";

    public final static String CONTINUES_PCOUNT = SCANNED_PCOUNT + "CONTINUOUS/";
    public final static String PER_CASE_PCOUNT = SCANNED_PCOUNT+ "CASE/";
    public final static String PER_PIECE_PCOUNT = SCANNED_PCOUNT+ "PIECE/";

    public final static String STOCK_COUNT = "/STOCK/";
    public final static String SCANNED_STOCK_COUNT = FTP_MASTER_FOLDER + "STOCK/";
    public final static String CONTINUES_STOCK_COUNT = SCANNED_STOCK_COUNT + "CONTINUOUS/";
    public final static String PER_CASE_STOCK_COUNT = SCANNED_STOCK_COUNT+ "CASE/";
    public final static String PER_PIECE_STOCK_COUNT = SCANNED_STOCK_COUNT+ "PIECE/";


    public final static String MMS_FTP_FOLDER = "/MMS/";
    public final static String MMS_RCR = MMS_FTP_FOLDER + "RCR/";
}
