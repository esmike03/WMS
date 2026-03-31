package com.zebra.waltermartmobilecollector.services;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

public final class Barcode {

    private static String filterAction = "";

    private static Listener listener;

    public static void init(Listener barcodeListener) {
        listener = barcodeListener;
    }

    private static void sendDataWedgeIntentWithExtra(Activity act, String action, String extraKey, Bundle extras) {
        Intent dwIntent = new Intent();
        dwIntent.setAction(action);
        dwIntent.putExtra(extraKey, extras);
        act.sendBroadcast(dwIntent);
    }

    private static void sendDataWedgeIntentWithExtra(Activity act, String action, String extraKey, String ext) {
        Intent dwIntent = new Intent();
        dwIntent.setAction(action);
        dwIntent.putExtra(extraKey, ext);
        act.sendBroadcast(dwIntent);
    }

    public static void createProfile(Activity act, String appName) {
        // DataWedge configuration
        sendDataWedgeIntentWithExtra(
                act,
                "com.symbol.datawedge.api.ACTION",
                "com.symbol.datawedge.api.CREATE_PROFILE",
                appName
        );

        Bundle configBundle = new Bundle();
        configBundle.putString("PROFILE_NAME", appName);
        configBundle.putString("PROFILE_ENABLED", "true");
        configBundle.putString("CONFIG_MODE", "CREATE_IF_NOT_EXIST");

        Bundle barcodeConfig = new Bundle();
        barcodeConfig.putString("PLUGIN_NAME", "BARCODE");
        barcodeConfig.putString("RESET_CONFIG", "true");

        Bundle params = new Bundle();

        params.putString("scanner_selection", "auto");
        params.putString("scanner_input_enabled", "true");

        params.putString("decoder_code128", "true");
        params.putString("decoder_datamatrix", "false");
        params.putString("decoder_code11", "true");
        params.putString("decoder_code39", "true");
        params.putString("decoder_ean13", "true");
        params.putString("decoder_ean8", "true");
        params.putString("decoder_korean_3of5", "true");
        params.putString("decoder_chinese_2of5", "true");
        params.putString("decoder_d2of5", "true");
        params.putString("decoder_trioptic39", "true");
        params.putString("decoder_code93", "true");
        params.putString("decoder_msi", "true");
        params.putString("decoder_codabar", "true");
        params.putString("decoder_upce0", "true");
        params.putString("decoder_upce0_report_check_digit", "true");
        params.putString("decoder_upce1", "true");
        params.putString("decoder_upca", "true");
        params.putString("decoder_us4state", "true");
        params.putString("decoder_tlc39", "true");
        params.putString("decoder_mailmark", "true");
        params.putString("decoder_hanxin", "true");
        params.putString("decoder_signature", "true");
        params.putString("decoder_webcode", "true");
        params.putString("decoder_matrix_2of5", "true");
        params.putString("decoder_i2of5", "true");
        params.putString("decoder_gs1_databar", "true");
        params.putString("decoder_qrcode", "false");
        params.putString("decoder_pdf417", "true");
        params.putString("decoder_composite_ab", "true");
        params.putString("decoder_composite_c", "true");
        params.putString("decoder_microqr", "true");
        params.putString("decoder_aztec", "true");
        params.putString("decoder_maxicode", "true");
        params.putString("decoder_micropdf", "true");
        params.putString("decoder_uspostnet", "true");
        params.putString("decoder_usplanet", "true");
        params.putString("decoder_australian_postal", "true");
        params.putString("decoder_uk_postal", "true");
        params.putString("decoder_japanese_postal", "true");
        params.putString("decoder_canadian_postal", "true");
        params.putString("decoder_dutch_postal", "true");

        barcodeConfig.putBundle("PARAM_LIST", params);
        configBundle.putBundle("PLUGIN_CONFIG", barcodeConfig);

        Bundle appConfig = new Bundle();
        appConfig.putString("PACKAGE_NAME", act.getPackageName());
        appConfig.putStringArray("ACTIVITY_LIST", new String[]{"*"});
        configBundle.putParcelableArray("APP_LIST", new Bundle[]{appConfig});

        sendDataWedgeIntentWithExtra(
                act,
                "com.symbol.datawedge.api.ACTION",
                "com.symbol.datawedge.api.SET_CONFIG",
                configBundle
        );

        /**  INTENT CONFIG  **/
        configBundle.remove("PLUGIN_CONFIG");

        Bundle intentConfig = new Bundle();
        intentConfig.putString("PLUGIN_NAME", "INTENT");
        intentConfig.putString("RESET_CONFIG", "true");
        Bundle intentProps = new Bundle();

        intentProps.putString("intent_output_enabled", "true");
        intentProps.putString("intent_action", act.getPackageName() + ".ACTION");
        intentProps.putString("intent_delivery", "2");
        intentConfig.putBundle("PARAM_LIST", intentProps);
        configBundle.putBundle("PLUGIN_CONFIG", intentConfig);

        sendDataWedgeIntentWithExtra(
                act,
                "com.symbol.datawedge.api.ACTION",
                "com.symbol.datawedge.api.SET_CONFIG",
                configBundle
        );

        /**  KEYSTROKE CONFIG  **/
        configBundle.remove("PLUGIN_CONFIG");

        Bundle keystrokeConfig = new Bundle();
        keystrokeConfig.putString("PLUGIN_NAME", "KEYSTROKE");
        keystrokeConfig.putString("RESET_CONFIG", "true");
        Bundle keystrokeProps = new Bundle();

        keystrokeProps.putString("keystroke_output_enabled", "false");
        keystrokeConfig.putBundle("PARAM_LIST", keystrokeProps);
        configBundle.putBundle("PLUGIN_CONFIG", keystrokeConfig);

        sendDataWedgeIntentWithExtra(
                act,
                "com.symbol.datawedge.api.ACTION",
                "com.symbol.datawedge.api.SET_CONFIG",
                configBundle
        );
    }

    private static final BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (!intent.getAction().equals(filterAction)) return;

                listener.onScanned(
                        intent.getStringExtra("com.symbol.datawedge.data_string"),
                        intent.getStringExtra("com.symbol.datawedge.label_type")
                );
            } catch (Exception e) {
                listener.onScannedError(e);
            }
        }
    };

    public static void registerReceivers(Activity act) {
        IntentFilter filter = new IntentFilter();
        filterAction = act.getPackageName() + ".ACTION";
        filter.addAction(filterAction);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            act.registerReceiver(myBroadcastReceiver, filter, Context.RECEIVER_EXPORTED);
        else
            act.registerReceiver(myBroadcastReceiver, filter);
    }

    public static void unregisterReceivers(Activity act) {
        act.unregisterReceiver(myBroadcastReceiver);
    }

    public interface Listener {
        void onScanned(String data, String labelType);

        void onScannedError(Exception e);
    }

}