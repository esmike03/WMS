package com.zebra.waltermartmobilecollector.activities.return_to_vendor;

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
        setContentView(R.layout.activity_rtv_view);

        ArrayList<Model> list = new ArrayList<>();
        Service.get(0, DataAdaptor.TOTAL_PER_LOAD, list);
        if (list.size() == 0) return;

        findViewById(R.id.txtNoData).setVisibility(View.GONE);

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