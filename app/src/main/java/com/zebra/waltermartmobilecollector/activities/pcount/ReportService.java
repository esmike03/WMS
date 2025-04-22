package com.zebra.waltermartmobilecollector.activities.pcount;

import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.activities.purchase_order.auto_matching.Model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public final class ReportService {

    public static AMModel getSingleWithoutPas3(ArrayList<Model> allData) {
        AMModel amModel = new AMModel();
        StringBuffer reportBodyBuffer = new StringBuffer();
        StringBuffer skuBodyBuffer = new StringBuffer();
        StringBuffer tempBuffer = new StringBuffer();

        String date = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            date = DateTimeFormatter.ofPattern("MM/dd/yyyy").format(LocalDateTime.now());

        int totalPas1 = 0, totalScannedSku = 0, skuCounter = 1;

        for (int i = 0; i < allData.size(); i++) {
            Model model = allData.get(i);

            totalPas1 += model.getPas1();

            tempBuffer
                    .append(model.getSku()).append(",'")
                    .append(model.getBarcode()).append(",")
                    .append(model.getDesc()).append(",")
                    .append(model.getPas1())
                    .append(",0,");

            if (model.getPas1() != 0) {
                tempBuffer.append("Y");
                totalScannedSku++;
            }

            tempBuffer
                    .append(",")
                    .append(model.getPas1()).append("\n");

            reportBodyBuffer.append(i + 1).append(",").append(tempBuffer);
            if (model.getPas1() != 0) {
                skuBodyBuffer.append(skuCounter).append(",").append(tempBuffer);
                skuCounter++;
            }
            tempBuffer.setLength(0);
        }

        String header = new StringBuffer()
                .append("Rundate : ").append(date)
                .append("\nSTORE CODE : ").append(Globals.getStoreCode())
                .append("\nLOCATION : ").append(Globals.selectedLocation)
                .append("\nTOTAL SKU with VARIANCE : 0\n")
                .append("TOTAL SKU with COUNT : ").append(totalScannedSku)
                .append("\nTOTAL PCS : ").append(totalPas1)
                .append("\n,,,SUB TOTAL : ,")
                .append(totalPas1)
                .append(",0,,")
                .append(totalPas1)
                .append("\nItem No,SKU,Barcode,Description,Pas1,Pas3,Mat Flg,Final Qty\n")
                .toString();

        amModel.setReport(header + reportBodyBuffer);
        amModel.setSkuReport(header + skuBodyBuffer);
        amModel.setTotalPcsExpected(totalPas1);

        return amModel;
    }

    public static AMModel getWithoutPas3(ArrayList<Model> allData) {
        AMModel amModel = new AMModel();
        StringBuffer reportBodyBuffer = new StringBuffer();
        StringBuffer skuBodyBuffer = new StringBuffer();
        StringBuffer tempBuffer = new StringBuffer();

        String date = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            date = DateTimeFormatter.ofPattern("MM/dd/yyyy").format(LocalDateTime.now());

        int totalScannedPcs = 0, totalPas1 = 0, totalPas2 = 0, totalScannedSku = 0, skuCounter = 1, skuVariance = 0;
        for (int i = 0; i < allData.size(); i++) {
            Model model = allData.get(i);

            if (model.getPas1() == model.getPas2()) {
                totalScannedPcs += model.getPas1();
            } else {
                amModel.unmatch();
                skuVariance++;
            }
            totalPas1 += model.getPas1();
            totalPas2 += model.getPas2();

            tempBuffer
                    .append(model.getSku()).append(",'")
                    .append(model.getBarcode()).append(",")
                    .append(model.getDesc()).append(",")
                    .append(model.getPas1()).append(",")
                    .append(model.getPas2()).append(",0,");

            if (model.getPas1() != 0 || model.getPas2() != 0) {
                tempBuffer.append(model.getPas1() == model.getPas2() ? "Y" : "N");
                totalScannedSku++;
            }

            tempBuffer
                    .append(",")
                    .append(
                            model.getPas1() == model.getPas2()
                                    ? model.getPas1()
                                    : 0
                    ).append("\n");

            reportBodyBuffer.append(i + 1).append(",").append(tempBuffer);
            if (model.getPas1() != 0 || model.getPas2() != 0) {
                skuBodyBuffer.append(skuCounter).append(",").append(tempBuffer);
                skuCounter++;
            }
            tempBuffer.setLength(0);
        }

        String header = new StringBuffer()
                .append("Rundate : ").append(date)
                .append("\nSTORE CODE : ").append(Globals.getStoreCode())
                .append("\nLOCATION : ").append(Globals.selectedLocation)
                .append("\nTOTAL SKU with VARIANCE :").append(skuVariance)
                .append("\nTOTAL SKU with COUNT : ").append(totalScannedSku)
                .append("\nTOTAL PCS : ").append(totalScannedPcs)
                .append("\n,,,SUB TOTAL : ,")
                .append(totalPas1).append(",")
                .append(totalPas2).append(",0,,")
                .append(totalScannedPcs)
                .append("\nItem No,SKU,Barcode,Description,Pas1,Pas2,Pas3,Mat Flg,Final Qty\n")
                .toString();

        amModel.setReport(header + reportBodyBuffer);
        amModel.setSkuReport(header + skuBodyBuffer);
        amModel.setTotalPcsExpected(totalScannedPcs);

        return amModel;
    }

    public static AMModel getSingle(ArrayList<Model> allData) {
        AMModel amModel = new AMModel();
        StringBuffer reportBodyBuffer = new StringBuffer();
        StringBuffer skuBodyBuffer = new StringBuffer();
        StringBuffer tempBuffer = new StringBuffer();
        StringBuffer finalTxtBuffer = new StringBuffer();
        int totalScannedPcs = 0, totalPas1 = 0, totalPas3 = 0, totalScannedSku = 0, skuCounter = 1, skuVariance = 0;
        for (int i = 0; i < allData.size(); i++) {
            Model model = allData.get(i);
            int pc = model.getPas3() > 0 ? model.getPas3() : model.getPas1();
            if (model.getPas3() > 0)
                skuVariance++;

            if (pc != 0)
                finalTxtBuffer
                        .append(Globals.getStoreCode()).append(",")
                        .append(Globals.selectedLocation).append(",")
                        .append(model.getBarcode()).append(",")
                        .append(pc).append("\n");

            totalScannedPcs += pc;
            totalPas1 += model.getPas1();
            totalPas3 += model.getPas3();

            tempBuffer
                    .append(model.getSku()).append(",'")
                    .append(model.getBarcode()).append(",")
                    .append(model.getDesc()).append(",")
                    .append(model.getPas1()).append(",")
                    .append(model.getPas3()).append(",");
            if (model.getPas1() != 0 || model.getPas3() != 0) {
                tempBuffer.append("Y");
                totalScannedSku++;
            }
            tempBuffer
                    .append(",")
                    .append(pc).append("\n");

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
                .append("Rundate : ").append(date)
                .append("\nSTORE CODE : ").append(Globals.getStoreCode())
                .append("\nLOCATION : ").append(Globals.selectedLocation)
                .append("\nTOTAL SKU with VARIANCE : ").append(skuVariance)
                .append("\nTOTAL SKU with COUNT : ").append(totalScannedSku)
                .append("\nTOTAL PCS : ").append(totalScannedPcs)
                .append("\n,,,SUB TOTAL : ,")
                .append(totalPas1).append(",")
                .append(totalPas3).append(",,")
                .append(totalScannedPcs)
                .append("\nItem No,SKU,Barcode,Description,Pas1,Pas3,Mat Flg,Final Qty\n")
                .toString();

        amModel.setReport(header + reportBodyBuffer);
        amModel.setSkuReport(header + skuBodyBuffer);
        amModel.setFinalTxt(finalTxtBuffer.toString());

        return amModel;
    }

    public static AMModel get(ArrayList<Model> allData) {
        AMModel amModel = new AMModel();
        StringBuffer reportBodyBuffer = new StringBuffer();
        StringBuffer skuBodyBuffer = new StringBuffer();
        StringBuffer tempBuffer = new StringBuffer();
        StringBuffer finalTxtBuffer = new StringBuffer();
        int totalScannedPcs = 0, totalPas1 = 0, totalPas2 = 0, totalPas3 = 0, totalScannedSku = 0,
                skuCounter = 1, skuVariance = 0;
        for (int i = 0; i < allData.size(); i++) {
            Model model = allData.get(i);
            int pc = model.getPas3() > 0 ? model.getPas3() : model.getPas1();

            if (model.getPas3() > 0)
                skuVariance++;

            if (pc != 0)
                finalTxtBuffer
                        .append(Globals.getStoreCode()).append(",")
                        .append(Globals.selectedLocation).append(",")
                        .append(model.getBarcode()).append(",")
                        .append(pc).append("\n");

            totalScannedPcs += pc;
            totalPas1 += model.getPas1();
            totalPas2 += model.getPas2();
            totalPas3 += model.getPas3();

            tempBuffer
                    .append(model.getSku()).append(",'")
                    .append(model.getBarcode()).append(",")
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
                    .append(pc).append("\n");

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

        String header = new StringBuffer()
                .append("Rundate : ").append(date)
                .append("\nSTORE CODE : ").append(Globals.getStoreCode())
                .append("\nLOCATION : ").append(Globals.selectedLocation)
                .append("\nTOTAL SKU with VARIANCE :").append(skuVariance)
                .append("\nTOTAL SKU with COUNT : ").append(totalScannedSku)
                .append("\nTOTAL PCS : ").append(totalScannedPcs)
                .append("\n,,,SUB TOTAL : ,")
                .append(totalPas1).append(",")
                .append(totalPas2).append(",")
                .append(totalPas3).append(",,")
                .append(totalScannedPcs)
                .append("\nItem No,SKU,Barcode,Description,Pas1,Pas2,Pas3,Mat Flg,Final Qty\n")
                .toString();

        amModel.setReport(header + reportBodyBuffer);
        amModel.setSkuReport(header + skuBodyBuffer);
        amModel.setFinalTxt(finalTxtBuffer.toString());

        return amModel;
    }

}
