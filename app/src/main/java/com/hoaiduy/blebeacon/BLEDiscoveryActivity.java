package com.hoaiduy.blebeacon;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.TextView;

import com.hoaiduy.blebeacon.presenter.DiscoverPresenter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by hoaiduy2503 on 8/22/2017.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BLEDiscoveryActivity extends AppCompatActivity {

    @BindView(R.id.recycleView)
    RecyclerView recyclerView;
    @BindView(R.id.btnScan)
    Button btnScan;
    @BindView(R.id.serviceData)
    TextView serviceData;

    RecyclerView.LayoutManager layoutManager;
    private DiscoverPresenter presenter;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);
        Intent intent = getIntent();
        intent.getAction();

        setupUI();
    }

    private void setupUI() {
        ButterKnife.bind(this);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        presenter = new DiscoverPresenter(this);
        btnScan.setOnClickListener(view -> {
            presenter.setupRecycleView(recyclerView);
        });

    }
}