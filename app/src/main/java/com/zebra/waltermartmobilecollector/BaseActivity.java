package com.zebra.waltermartmobilecollector;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.zebra.waltermartmobilecollector.activities.MainActivity;
import com.zebra.waltermartmobilecollector.interfaces.ThreadRunnable;
import com.zebra.waltermartmobilecollector.services.FTP;

public class BaseActivity extends AppCompatActivity {

    public static Class<?> backInto;
    public final String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setActionBarTitleCustom();

        Helper.setDialog(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        hideSystemUI();
    }

    private void setActionBarTitleCustom() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;

        LinearLayout linearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        linearLayout.setLayoutParams(linearLayoutParams);

        TextView appName = new TextView(this);
        appName.setText(R.string.app_name);
        appName.setTextSize(20);
        appName.setTypeface(Typeface.DEFAULT_BOLD);
        appName.setTextColor(Color.WHITE);
        appName.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        appName.setLayoutParams(param);

        TextView user = new TextView(this);
        user.setText("User : " + Globals.name);
        user.setTextSize(17);
        user.setTypeface(Typeface.DEFAULT_BOLD);
        user.setTextColor(Color.WHITE);
        user.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams param2 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        param2.setMarginStart(20);
        user.setLayoutParams(param2);

        linearLayout.addView(appName);
        linearLayout.addView(user);

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(linearLayout);
//            getSupportActionBar().setTitle("WMS AIOS     User : " + Globals.name);
    }

    // This method hides both the navigation bar and status bar
    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION   // Hides the navigation bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY // Makes sure the navigation bar stays hidden
        );
    }

    // To make sure the navigation bar stays hidden when the user interacts with the screen
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    public void onClickHome(View v) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public int validateQty(EditText editTextQty) {
        String qtyStr = editTextQty.getText().toString().trim();
        if (qtyStr.isEmpty()) {
            editTextQty.setError("This is required");
            return -1;
        }

        int newQ = Integer.parseInt(qtyStr);
        if (newQ < 1) {
            showError("Entered quantity must be greater than 0!!!");
            return -1;
        }

        return newQ;
    }

    public void hideKeyboard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public void onClickBack(View v) {
        startActivity(new Intent(this, backInto));
        finish();
    }

    public void showError(String message) {
        Helper.showError(message);
    }

    public void showErrorInThread(String message) {
        runOnUiThread(() -> Helper.showError(message));
    }

    public void runThread(ThreadRunnable runnable) {
        Helper.showLoading();
        new Thread(() -> {
            try {
                FTP.login();
                runnable.run();
            } catch (Exception e) {
                e.printStackTrace();
                showErrorInThread(e.getMessage());
            } finally {
                try {
                    FTP.disconnect();
                } catch (Exception e) {
                }
                runOnUiThread(() -> Helper.closeLoading());
            }
        }).start();
    }

    public void showSuccessInThread(String message) {
        runOnUiThread(() -> Helper.showSuccess(message));
    }

    public void showSuccess(String message) {
        Helper.showSuccess(message);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog
                .Builder(this)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to exit this application?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    super.onBackPressed();
                })
                .show();
    }
}
