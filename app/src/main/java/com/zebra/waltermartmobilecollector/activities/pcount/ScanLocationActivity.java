package com.zebra.waltermartmobilecollector.activities.pcount;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.zebra.waltermartmobilecollector.Folders;
import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.R;
import com.zebra.waltermartmobilecollector.ScanBaseActivity;

public class ScanLocationActivity extends ScanBaseActivity {

    private String location = "";
    private TextView instruct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_count_location_scan);

        ((TextView) findViewById(R.id.text9)).setText("PCOUNT");
        instruct = findViewById(R.id.txtInstruction);

        if (Globals.stockCountOption.equals("2"))
            ((TextView) findViewById(R.id.txtType)).setText("PER PIECE");
        else if (Globals.stockCountOption.equals("3"))
            ((TextView) findViewById(R.id.txtType)).setText("PER CASE");

        createBarcodeHandler();
    }

    @Override
    public boolean scanProcess(String data) {
        location = data.trim();
        instruct.setText(location);
        return true;
    }

    public void onOK(View _) {
        if (location.isEmpty()) {
            showError("Location is required to continue!!!");
            return;
        }

        if (!Character.isLetter(location.charAt(0)) || !location.matches("[a-zA-Z0-9]+")) {
            showError("INVALID LOCATION!!!");
            return;
        }

        if (!Service.locationExists(location)){
            showError("Unable to find location!!!");
            return;
        }

        if (Service.locationIsScanned(location)){
            showError("You scanned this location in another scanning type!!!");
            return;
        }

        runThread(() -> {
            if (Service.locationIsDone(location, Folders.MATCHED_PCOUNT) || Service.locationIsDone(location, Folders.UNMATCHED_PCOUNT)) {
                showErrorInThread("This location is already processed!!!");
                return;
            }

            if (Service.locationIsOver(location, Folders.SCANNED_PCOUNT)) {
                showErrorInThread("This location is already scanned!!!");
                return;
            }

            Globals.selectedLocation = location;
            runOnUiThread(() -> {
                startActivity(new Intent(this, ScanActivity.class));
                finish();
            });
        });
    }

    @Override
    public void setManualInputInputType(EditText input) {
        input.setInputType(InputType.TYPE_CLASS_TEXT);
    }

}