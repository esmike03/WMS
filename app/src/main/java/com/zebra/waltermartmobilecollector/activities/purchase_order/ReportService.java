package com.zebra.waltermartmobilecollector.activities.purchase_order;

import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.activities.purchase_order.auto_matching.Model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public final class ReportService {

    public static AMModel getWDSWithoutPas3(ArrayList<Model> allData, String poNo) {
        AMModel amModel = new AMModel();
        StringBuffer reportBodyBuffer = new StringBuffer();
        StringBuffer skuBodyBuffer = new StringBuffer();
        StringBuffer tempBuffer = new StringBuffer();

        String date = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            date = DateTimeFormatter.ofPattern("MM/dd/yyyy").format(LocalDateTime.now());

        int totalScannedBox = 0, totalScannedPcs = 0, totalPas1 = 0, totalScannedSku = 0,
                totalPas1Box = 0, skuCounter = 1,
                totalPas1Diff = 0, totalBoxExpected = 0, totalPcsExpected = 0;

        for (int i = 0; i < allData.size(); i++) {
            Model model = allData.get(i);

            int expBox = model.getPcs() / model.getFactor();
            int pas1Box = model.getPas1() / model.getFactor();
            totalBoxExpected += expBox;
            totalPcsExpected += model.getPcs();
            totalPas1Box += pas1Box;
            totalPas1Diff += (expBox - pas1Box);

            totalScannedBox += (model.getPas1() / model.getFactor());
            totalScannedPcs += model.getPas1();
            totalPas1 += model.getPas1();

            tempBuffer
                    .append(model.getSku()).append(",")
                    .append("'").append(model.getBarcode()).append(",")
                    .append(model.getDesc()).append(",")
                    .append(model.getPas1()).append(",")
                    .append("0,");

            if (model.getPas1() != 0) {
                tempBuffer.append("Y");
                totalScannedSku++;
            }

            tempBuffer
                    .append(",")
                    .append(model.getPas1()).append(",")
                    .append(model.getPcs()).append(",")
                    .append(expBox).append(",")
                    .append(pas1Box).append(",")
                    .append(expBox - pas1Box).append("\n");

            reportBodyBuffer.append(i + 1).append(",").append(tempBuffer);
            if (model.getPas1() != 0) {
                skuBodyBuffer.append(skuCounter).append(",").append(tempBuffer);
                skuCounter++;
            }
            tempBuffer.setLength(0);
        }

        String header = new StringBuffer()
                .append("Rundate : ").append(date).append("\n")
                .append("PURCHASE ORDER NO : ").append(poNo).append("\n")

                .append("TOTAL SKU with COUNT : ").append(totalScannedSku).append("\n")
                .append("EXPECTED TOTAL BOXES : ").append(totalBoxExpected).append("\n")
                .append("TOTAL BOX : ").append(totalScannedBox).append("\n")
                .append("TOTAL PCS : ").append(totalScannedPcs).append("\n")
                .append("LOCATION : ").append(Globals.getStoreCode())
                .append(",,,SUB TOTAL : ,")
                .append(totalPas1).append(",")
                .append("0,,")
                .append(totalScannedPcs).append(",")
                .append(totalPcsExpected).append(",")
                .append(totalBoxExpected).append(",")
                .append(totalPas1Box).append(",")
                .append(totalPas1Diff).append("\n")
                .append("Item No,SKU,Barcode,Description,Pas1,Pas3,Mat Flg,Final Qty,Order,PO Box,Pas1 box,Pas1 Diff\n")
                .toString();

        amModel.setReport(header + reportBodyBuffer);
        amModel.setSkuReport(header + skuBodyBuffer);
        amModel.setTotalBoxExpected(totalBoxExpected);
        amModel.setTotalPcsExpected(totalPcsExpected);

        return amModel;
    }

    public static AMModel getWithoutPas3(ArrayList<Model> allData, String poNo, String siNum) {
        AMModel amModel = new AMModel();
        StringBuffer reportBodyBuffer = new StringBuffer();
        StringBuffer skuBodyBuffer = new StringBuffer();
        StringBuffer tempBuffer = new StringBuffer();
        StringBuffer receiptBuilder = new StringBuffer();

        String date = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            date = DateTimeFormatter.ofPattern("yy/MM/dd").format(LocalDateTime.now());

        int totalScannedBox = 0, totalScannedPcs = 0, totalPas1 = 0, totalPas2 = 0, totalScannedSku = 0,
                totalDiff = 0, totalPas1Box = 0, totalPas2Box = 0, skuCounter = 1,
                totalPas1Diff = 0, totalPas2Diff = 0, totalBoxExpected = 0, totalPcsExpected = 0;
        for (int i = 0; i < allData.size(); i++) {
            Model model = allData.get(i);

            int expBox = model.getPcs() / model.getFactor();
            int pas1Box = model.getPas1() / model.getFactor();
            int pas2Box = model.getPas2() / model.getFactor();
            int diff = model.getPas1() - model.getPas2();
            if (diff < 0)
                diff = -diff;
            totalBoxExpected += expBox;
            totalPcsExpected += model.getPcs();
            totalDiff += diff;
            totalPas1Box += pas1Box;
            totalPas2Box += pas2Box;
            totalPas1Diff += (expBox - pas1Box);
            totalPas2Diff += (expBox - pas2Box);

            if (model.getPas1() == model.getPas2()) {
                totalScannedBox += (model.getPas1() / model.getFactor());
                totalScannedPcs += model.getPas1();
            } else amModel.unmatch();
            totalPas1 += model.getPas1();
            totalPas2 += model.getPas2();

            tempBuffer
                    .append(model.getSku()).append(",")
                    .append("'").append(model.getBarcode()).append(",")
                    .append(model.getDesc()).append(",")
                    .append(model.getPas1()).append(",")
                    .append(model.getPas2()).append(",")
                    .append("0,");

            if (model.getPas1() != 0 || model.getPas2() != 0) {
                tempBuffer.append(model.getPas1() == model.getPas2() ? "Y" : "N");
                totalScannedSku++;
            }

            tempBuffer
                    .append(",")
                    .append(model.getPas1() == model.getPas2() ? model.getPas1() : 0).append(",")
                    .append(model.getPcs()).append(",")
                    .append(diff).append(",")
                    .append(expBox).append(",")
                    .append(pas1Box).append(",")
                    .append(expBox - pas1Box).append(",")
                    .append(pas2Box).append(",")
                    .append(expBox - pas2Box).append(",")
                    .append(model.getPas1Username() != null ? model.getPas1Username() : "").append(",")
                    .append(model.getPas1Date() != null ? model.getPas1Date() : "").append(",")
                    .append(model.getPas2Username() != null ? model.getPas2Username() : "").append(",")
                    .append(model.getPas2Date() != null ? model.getPas2Date() : "").append("\n");

            reportBodyBuffer.append(i + 1).append(",").append(tempBuffer);
            if (model.getPas1() != 0 || model.getPas2() != 0) {
                receiptBuilder
                        .append(model.getSku()).append(",'")
                        .append(model.getBarcode()).append(",")
                        .append(model.getDesc()).append(",")
                        .append(model.getPas1()).append("\n");
                skuBodyBuffer.append(skuCounter).append(",").append(tempBuffer);
                skuCounter++;
            }
            tempBuffer.setLength(0);
        }

        String header = new StringBuffer()
                .append("Rundate : ").append(date).append("\n")
                .append("PURCHASE ORDER NO : ").append(poNo).append("\n")
                .append("SI # : ").append(siNum).append("\n")
                .append("TOTAL SKU with COUNT : ").append(totalScannedSku).append("\n")
                .append("EXPECTED TOTAL BOXES : ").append(totalBoxExpected).append("\n")
                .append("TOTAL BOX : ").append(totalScannedBox).append("\n")
                .append("TOTAL PCS : ").append(totalScannedPcs).append("\n")
                .append("LOCATION : ").append(Globals.getStoreCode())
                .append(",,,SUB TOTAL : ,")
                .append(totalPas1).append(",")
                .append(totalPas2).append(",")
                .append("0,,")
                .append(totalScannedPcs).append(",")
                .append(totalPcsExpected).append(",")
                .append(totalDiff).append(",")
                .append(totalBoxExpected).append(",")
                .append(totalPas1Box).append(",")
                .append(totalPas1Diff).append(",")
                .append(totalPas2Box).append(",")
                .append(totalPas2Diff).append("\n")
                .append("Item No,SKU,Barcode,Description,Pas1,Pas2,Pas3,Mat Flg,Final Qty,Order,Diff,PO Box,Pas1 box,Pas1 Diff,Pas2 box,Pas2 Diff,P1 User,P1 Date,P2 User,P2 Date\n")
                .toString();

        if (amModel.isMatched()) {
            StringBuilder rb = new StringBuilder();
            rb
                    .append("STOCK RECEIVING SUMMARY\n")
                    .append("Waltermart Supermarket Inc.\n")
                    .append("003-501-787-000\n\n")
                    .append(",,,RCR No:\n\n")
                    .append("Vendor :,______________________,,Delivery Date:")
                    .append(date)
                    .append("\n,______________________,,PO # :").append(poNo)
                    .append("\n\nEXPECTED TOTAL BOXES:").append(totalBoxExpected)
                    .append(",,,Actual Total Boxes:").append(totalScannedBox)
                    .append("\n,,,Actual SKU Count:").append(totalScannedSku)
                    .append("\n\nSKU,Barcode,Description,Final QTY\n")
                    .append(receiptBuilder)
                    .append(",,,TOTAL:").append(totalPas1)
                    .append("\n\n__________________,,________________\nName/Signature/Date,,Name/Signature/Date\n")
                    .append("Delivery Crew,,WMS/WDS Receiving\n\n\n")
                    .append("__________________,,________________\nName/Signature/Date,,Name/Signature/Date\n")
                    .append("Security Guard,,WMS/WDS Receiving");
            amModel.setReceipt(rb.toString());
        }

        amModel.setReport(header + reportBodyBuffer);
        amModel.setSkuReport(header + skuBodyBuffer);
        amModel.setTotalBoxExpected(totalBoxExpected);
        amModel.setTotalPcsExpected(totalPcsExpected);

        return amModel;
    }

    public static AMModel getWDS(ArrayList<Model> allData, String poNo) {
        AMModel amModel = new AMModel();
        StringBuffer reportBodyBuffer = new StringBuffer();
        StringBuffer skuBodyBuffer = new StringBuffer();
        StringBuffer tempBuffer = new StringBuffer();
        StringBuffer finalTxtBuffer = new StringBuffer();
        int totalScannedBox = 0, totalScannedPcs = 0, totalPas1 = 0, totalPas3 = 0, totalScannedSku = 0,
                totalPas1Box = 0, skuCounter = 1,
                totalPas1Diff = 0, totalBoxExpected = 0, totalPcsExpected = 0;
        for (int i = 0; i < allData.size(); i++) {
            Model model = allData.get(i);
            int pc = model.getPas3() > 0 ? model.getPas3() : model.getPas1();

            int expBox = model.getPcs() / model.getFactor();
            int pas1Box = model.getPas1() / model.getFactor();
            totalPas1Box += pas1Box;
            totalPas1Diff += (expBox - pas1Box);
            totalBoxExpected += expBox;
            totalPcsExpected += model.getPcs();

            if (pc != 0)
                finalTxtBuffer
                        .append(poNo).append(",")
                        .append(model.getSku()).append(",")
                        .append(pc).append("\n");

            totalScannedBox += (pc / model.getFactor());
            totalScannedPcs += pc;
            totalPas1 += model.getPas1();
            totalPas3 += model.getPas3();

            tempBuffer
                    .append(model.getSku()).append(",")
                    .append("'").append(model.getBarcode()).append(",")
                    .append(model.getDesc()).append(",")
                    .append(model.getPas1()).append(",")
                    .append(model.getPas3()).append(",");
            if (model.getPas1() != 0 || model.getPas3() != 0) {
                tempBuffer.append("Y");
                totalScannedSku++;
            }
            tempBuffer
                    .append(",")
                    .append(pc).append(",")
                    .append(model.getPcs()).append(",")
                    .append(expBox).append(",")
                    .append(pas1Box).append(",")
                    .append(expBox - pas1Box).append("\n");

            reportBodyBuffer.append(i + 1).append(",").append(tempBuffer);
            if (model.getPas1() != 0 || model.getPas3() != 0) {
                skuBodyBuffer.append(skuCounter).append(",").append(tempBuffer);
                skuCounter++;
            }
            tempBuffer.setLength(0);
        }

        String date = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            date = DateTimeFormatter.ofPattern("MM/dd/yyyy").format(LocalDateTime.now());

        String header = new StringBuffer()
                .append("Rundate : ").append(date).append("\n")
                .append("PURCHASE ORDER NO : ").append(poNo).append("\n")
                .append("TOTAL SKU with COUNT : ").append(totalScannedSku).append("\n")
                .append("EXPECTED TOTAL BOXES : ").append(totalBoxExpected).append("\n")
                .append("TOTAL BOX : ").append(totalScannedBox).append("\n")
                .append("TOTAL PCS : ").append(totalScannedPcs).append("\n")
                .append("LOCATION : ").append(Globals.getStoreCode())
                .append(",,,SUB TOTAL : ,")
                .append(totalPas1).append(",")
                .append(totalPas3).append(",,")
                .append(totalScannedPcs).append(",")
                .append(totalPcsExpected).append(",")
                .append(totalBoxExpected).append(",")
                .append(totalPas1Box).append(",")
                .append(totalPas1Diff).append("\n")
                .append("Item No,SKU,Barcode,Description,Pas1,Pas3,Mat Flg,Final Qty,Order,PO Box,Pas1 box,Pas1 Diff\n")
                .toString();

        amModel.setReport(header + reportBodyBuffer);
        amModel.setSkuReport(header + skuBodyBuffer);
        amModel.setFinalTxt(finalTxtBuffer.toString());

        return amModel;
    }

    public static AMModel get(ArrayList<Model> allData, String poNo, String siNum) {
        AMModel amModel = new AMModel();
        StringBuffer reportBodyBuffer = new StringBuffer();
        StringBuffer skuBodyBuffer = new StringBuffer();
        StringBuffer tempBuffer = new StringBuffer();
        StringBuffer finalTxtBuffer = new StringBuffer();
        StringBuffer receiptBuilder = new StringBuffer();
        int totalScannedBox = 0, totalScannedPcs = 0, totalPas1 = 0, totalPas2 = 0, totalPas3 = 0, totalScannedSku = 0,
                totalDiff = 0, totalPas1Box = 0, totalPas2Box = 0, skuCounter = 1,
                totalPas1Diff = 0, totalPas2Diff = 0, totalBoxExpected = 0, totalPcsExpected = 0;
        for (int i = 0; i < allData.size(); i++) {
            Model model = allData.get(i);
            int pc = model.getPas3() > 0 ? model.getPas3() : model.getPas1();

            int expBox = model.getPcs() / model.getFactor();
            int pas1Box = model.getPas1() / model.getFactor();
            int pas2Box = model.getPas2() / model.getFactor();
            int diff = model.getPas1() - model.getPas2();
            if (diff < 0)
                diff = -diff;
            totalDiff += diff;
            totalPas1Box += pas1Box;
            totalPas2Box += pas2Box;
            totalPas1Diff += (expBox - pas1Box);
            totalPas2Diff += (expBox - pas2Box);
            totalBoxExpected += expBox;
            totalPcsExpected += model.getPcs();

            if (pc != 0) {
                finalTxtBuffer
                        .append(poNo).append(",")
                        .append(model.getSku()).append(",")
                        .append(pc).append(",")
                        .append(model.getPas1Username() != null ? model.getPas1Username() : "").append(",")
                        .append(model.getPas2Username() != null ? model.getPas2Username() : "").append("\n");
                receiptBuilder
                        .append(model.getSku()).append(",'")
                        .append(model.getBarcode()).append(",")
                        .append(model.getDesc()).append(",")
                        .append(pc).append("\n");
            }

            totalScannedBox += (pc / model.getFactor());
            totalScannedPcs += pc;
            totalPas1 += model.getPas1();
            totalPas2 += model.getPas2();
            totalPas3 += model.getPas3();

            tempBuffer
                    .append(model.getSku()).append(",")
                    .append("'").append(model.getBarcode()).append(",")
                    .append(model.getDesc()).append(",")
                    .append(model.getPas1()).append(",")
                    .append(model.getPas2()).append(",")
                    .append(model.getPas3()).append(",");
            if (model.getPas1() != 0 || model.getPas2() != 0 || model.getPas3() != 0) {
                tempBuffer.append("Y");
                totalScannedSku++;
            }
            tempBuffer
                    .append(",")
                    .append(pc).append(",")
                    .append(model.getPcs()).append(",")
                    .append(diff).append(",")
                    .append(expBox).append(",")
                    .append(pas1Box).append(",")
                    .append(expBox - pas1Box).append(",")
                    .append(pas2Box).append(",")
                    .append(expBox - pas2Box).append(",")
                    .append(model.getPas1Username() != null ? model.getPas1Username() : "").append(",")
                    .append(model.getPas1Date() != null ? model.getPas1Date() : "").append(",")
                    .append(model.getPas2Username() != null ? model.getPas2Username() : "").append(",")
                    .append(model.getPas2Date() != null ? model.getPas2Date() : "").append("\n");

            reportBodyBuffer.append(i + 1).append(",").append(tempBuffer);
            if (model.getPas1() != 0 || model.getPas2() != 0 || model.getPas3() != 0) {
                skuBodyBuffer.append(skuCounter).append(",").append(tempBuffer);
                skuCounter++;
            }
            tempBuffer.setLength(0);
        }

        String date = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            date = DateTimeFormatter.ofPattern("MM/dd/yyyy").format(LocalDateTime.now());

        StringBuilder rb = new StringBuilder();
        rb
                .append("STOCK RECEIVING SUMMARY\n")
                .append("Waltermart Supermarket Inc.\n")
                .append("003-501-787-000\n\n")
                .append(",,,RCR No:\n\n")
                .append("Vendor :,______________________,,Delivery Date:")
                .append(date)
                .append("\n,______________________,,PO # :").append(poNo)
                .append("\n\nEXPECTED TOTAL BOXES:").append(totalBoxExpected)
                .append(",,,Actual Total Boxes:").append(totalScannedBox)
                .append("\n,,,Actual SKU Count:").append(totalScannedSku)
                .append("\n\nSKU,Barcode,Description,Final QTY\n")
                .append(receiptBuilder)
                .append(",,,TOTAL:").append(totalScannedPcs)
                .append("\n\n__________________,,________________\nName/Signature/Date,,Name/Signature/Date\n")
                .append("Delivery Crew,,WMS/WDS Receiving\n\n\n")
                .append("__________________,,________________\nName/Signature/Date,,Name/Signature/Date\n")
                .append("Security Guard,,WMS/WDS Receiving");
        amModel.setReceipt(rb.toString());

        String header = new StringBuffer()
                .append("Rundate : ").append(date).append("\n")
                .append("PURCHASE ORDER NO : ").append(poNo).append("\n")
                .append("SI # : ").append(siNum).append("\n")
                .append("TOTAL SKU with COUNT : ").append(totalScannedSku).append("\n")
                .append("EXPECTED TOTAL BOXES : ").append(totalBoxExpected).append("\n")
                .append("TOTAL BOX : ").append(totalScannedBox).append("\n")
                .append("TOTAL PCS : ").append(totalScannedPcs).append("\n")
                .append("LOCATION : ").append(Globals.getStoreCode())
                .append(",,,SUB TOTAL : ,")
                .append(totalPas1).append(",")
                .append(totalPas2).append(",")
                .append(totalPas3).append(",,")
                .append(totalScannedPcs).append(",")
                .append(totalPcsExpected).append(",")
                .append(totalDiff).append(",")
                .append(totalBoxExpected).append(",")
                .append(totalPas1Box).append(",")
                .append(totalPas1Diff).append(",")
                .append(totalPas2Box).append(",")
                .append(totalPas2Diff).append("\n")
                .append("Item No,SKU,Barcode,Description,Pas1,Pas2,Pas3,Mat Flg,Final Qty,Order,Diff,PO Box,Pas1 box,Pas1 Diff,Pas2 box,Pas2 Diff,P1 User,P1 Date,P2 User,P2 Date\n")
                .toString();

        amModel.setReport(header + reportBodyBuffer);
        amModel.setSkuReport(header + skuBodyBuffer);
        amModel.setFinalTxt(finalTxtBuffer.toString());

        return amModel;
    }

}
