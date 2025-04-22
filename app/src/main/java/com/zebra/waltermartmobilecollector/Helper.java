package com.zebra.waltermartmobilecollector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public final class Helper {

    private static AlertDialog dialog, errorDialog, successDialog;
    private static TextView error;
    private static ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_DTMF, 100);
    private static ToneGenerator stoneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);

    public static void setDialog(Activity act) {
        setLoadingDialog(act);
        setErrorDialog(act);
        setSuccessDialog(act);
    }

    public static void setLoadingDialog(Activity act) {
        dialog = new AlertDialog
                .Builder(act)
                .setCancelable(false)
                .setView(act.getLayoutInflater().inflate(R.layout.custom_loading_dialog, null))
                .create();
    }

    public static void setErrorDialog(Activity act) {
        View view = act.getLayoutInflater().inflate(R.layout.custom_error_dialog, null);
        view.findViewById(R.id.btnSub).setOnClickListener(v -> {
            errorDialog.dismiss();
        });
        error = view.findViewById(R.id.txtPas3SKU);
        errorDialog = new AlertDialog
                .Builder(act)
                .setCancelable(false)
                .setView(view)
                .create();
    }

    public static void setSuccessDialog(Activity act) {
        successDialog = new AlertDialog
                .Builder(act)
                .setTitle("Success")
                .setCancelable(false)
                .setPositiveButton("OK", null)
                .create();
    }

    public static void showError(Activity activity, Class<?> backTo, String message) {
        new AlertDialog
                .Builder(activity)
                .setTitle("Error")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", (dialogInterface, i) -> {
                    activity.startActivity(new Intent(activity, backTo));
                    activity.finish();
                })
                .show();
    }

    public static void showLoading() {
        dialog.show();
    }

    public static void showError(String message) {
        toneGenerator.startTone(ToneGenerator.TONE_DTMF_5, 300);
        error.setText(message);
        errorDialog.show();
    }

    public static void showSuccess(String message) {
        stoneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 180);
        successDialog.setMessage(message);
        successDialog.show();
    }

    public static void closeLoading() {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
    }

//    public static String getPOFilenameAndPath(String po, int number) {
//        return Folders.SCANNED_PO + po + "_" + number + "_" + Globals.name + ".txt";
//    }

    public static boolean isOutright(String type, String cpotag) {
        return (type.equals("01") || ((type.equalsIgnoreCase("cc") || type.equalsIgnoreCase("mc")) && cpotag.equalsIgnoreCase("cpo")));
    }

    private static StringBuilder cStr = new StringBuilder();

//    public static List<String> split(String str) {
//        List<String> result = new ArrayList<>();
//        boolean lastIsQuote = false;
//
//        for (int i = 0; i < str.length(); i++) {
//            char cChar = str.charAt(i);
//
//            if (cChar == '\"')
//                lastIsQuote = true;
//            if (cChar == '|') {
//                if (lastIsQuote)
//                    result.add(cStr.toString().substring(0, str.length() - 1));
//                else
//                    result.add(cStr.toString());
//                lastIsQuote = false;
//                cStr.setLength(0);
//            } else {
//                if (!lastIsQuote || (lastIsQuote && cStr.length() != 0))
//                    cStr.append(cChar);
//                lastIsQuote = false;
//            }
//        }
//        result.add(cStr.toString());
//        cStr.setLength(0);
//
//        return result;
//    }

    public static List<String> split(String str) {
        List<String> result = new ArrayList<>();
        boolean hasFQ = false, hasLQ = false;

        for (int i = 0; i < str.length(); i++) {
            char cChar = str.charAt(i);

            if (cChar == '\"') {
                if (cStr.length() == 0 && !hasFQ)
                    hasFQ = true;
                else if (hasFQ && !hasLQ)
                    hasLQ = true;
                else cStr.append("\"");
                continue;
            }

            if (cChar != '|') {
                if (hasLQ) {
                    hasLQ = false;
                    cStr.append("\"");
                }

                cStr.append(cChar);
                continue;
            }

            if (hasFQ && !hasLQ) {
                cStr.append(cChar);
                continue;
            }

            result.add(cStr.toString());
            hasLQ = hasFQ = false;
            cStr.setLength(0);
        }
        result.add(cStr.toString());
        cStr.setLength(0);

        return result;
    }

    public static int convertToIntAndRemoveDot(String str) {
        if (str == null) return 0;
        try {
            return Integer.parseInt(str.split("\\.")[0]);
        } catch (Exception e) {
            return 0;
        }
    }

}
