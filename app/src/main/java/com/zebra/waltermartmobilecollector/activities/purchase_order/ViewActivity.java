package com.zebra.waltermartmobilecollector.activities.purchase_order;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.waltermartmobilecollector.BaseActivity;
import com.zebra.waltermartmobilecollector.R;

import java.util.ArrayList;

public class ViewActivity extends BaseActivity {

    private DataAdaptor adaptor;
    private int firstVisibleItemPos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_po_view);

//        ArrayList<Model> list = new ArrayList<>();
//        Service.get(0, DataAdaptor.TOTAL_PER_LOAD, list);
//        if (list.size() == 0) return;

        if (!Service.hasScanned()) return;

        findViewById(R.id.txtNoData).setVisibility(View.GONE);

        RecyclerView rv = findViewById(R.id.rvSKUCount);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new POCountAdaptor(Service.getPOCount()));
        rv.setVisibility(View.VISIBLE);


        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adaptor = new DataAdaptor();
        recyclerView.setAdapter(adaptor);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if ((layoutManager.getChildCount() + layoutManager.findFirstVisibleItemPosition()) >= layoutManager.getItemCount()
                        && layoutManager.findFirstVisibleItemPosition() >= 0
                        && firstVisibleItemPos != layoutManager.findFirstVisibleItemPosition()) {
                    firstVisibleItemPos = layoutManager.findFirstVisibleItemPosition();
                    recyclerView.post(() -> adaptor.loadMore());
                }
            }
        });
    }
}