package com.zebra.waltermartmobilecollector.activities.stock_count;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.waltermartmobilecollector.BaseActivity;
import com.zebra.waltermartmobilecollector.R;

import java.util.ArrayList;

public class ViewActivity extends BaseActivity {

    private CountAdaptor adaptor;
    private boolean hasData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_count_view);

        ArrayList<RVModel> list = new ArrayList<>();
        Service.getCount(list);
        if (list.size() == 0) return;

        hasData = true;
        findViewById(R.id.txtNoData).setVisibility(View.GONE);

        RecyclerView rv = findViewById(R.id.rvCount);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adaptor = new CountAdaptor(list);
        rv.setAdapter(adaptor);
        rv.setVisibility(View.VISIBLE);

        EditText search = findViewById(R.id.edtTxtSearch);
        search.setOnKeyListener((vv, i, keyEvent) -> {
            if (i != EditorInfo.IME_ACTION_SEARCH && i != EditorInfo.IME_ACTION_DONE && keyEvent.getKeyCode() != KeyEvent.KEYCODE_ENTER)
                return false;
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN) return false;

            String find = search.getText().toString().trim();
            if (find.isEmpty()) {
                list.clear();
                Service.getCount(list);
                adaptor.notifyDataSetChanged();
                hideKeyboard(search);
                return false;
            }

            Service.getCountWithSearch(list, find);
            adaptor.notifyDataSetChanged();
            hideKeyboard(search);
            return false;
        });

//        Barcode.init(new Barcode.Listener() {
//            @Override
//            public void onScanned(String data, String labelType) {
//                String formattedData = data.trim();
//                while (formattedData.length() > 2 && formattedData.charAt(0) == '0')
//                    formattedData = formattedData.substring(1);
//
//                String find = search.getText().toString().trim();
//                if (find.isEmpty()) {
//                    Service.getCountWithBarcode(list, formattedData);
//                    adaptor.notifyDataSetChanged();
//                    return;
//                }
//
//                Service.getCount(list, find, formattedData);
//                adaptor.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onScannedError(Exception e) {
//                showError("Error scanning this barcode!!!");
//            }
//        });
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        if (hasData)
//            Barcode.registerReceivers(this);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        if (hasData)
//            Barcode.unregisterReceivers(this);
//    }
}