package com.hoaiduy.blebeacon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.hoaiduy.blebeacon.presenter.DiscoverPresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    @BindView(R.id.serviceUUID)
    TextView serviceUUID;

    RecyclerView.LayoutManager layoutManager;
    private DiscoverPresenter presenter;
    private List<ScanFilter> mDeviceList = new ArrayList<ScanFilter>();

    private ParcelUuid uuid;
    private Handler mHandler;
    private BluetoothAdapter mBtAdapter = null;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);
        Intent intent = getIntent();
        intent.getAction();
        final BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter = btManager.getAdapter();

        uuid = new ParcelUuid(UUID.fromString(getString(R.string.uuid)));
        mHandler = new Handler();
        setupUI();
        try {
            Scan(true);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void setupUI() {
        ButterKnife.bind(this);
//        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
//        recyclerView.setLayoutManager(layoutManager);
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
//        presenter = new DiscoverPresenter(this);
//        presenter.setupRecycleView(recyclerView);
        btnScan.setOnClickListener(view -> {
            Scan(true);
            mDeviceList.clear();
        });

    }

    private void Scan(boolean enable){
        ScanFilter filter = new ScanFilter.Builder()
                .setServiceUuid(uuid)
                .build();
        mDeviceList.add(filter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build();
        try {
            if (enable){
                mHandler.postDelayed(() -> mBtAdapter.getBluetoothLeScanner().stopScan(mCallback), 500);
                mBtAdapter.getBluetoothLeScanner().startScan(mDeviceList, settings, mCallback);
            }else {
                mBtAdapter.getBluetoothLeScanner().stopScan(mCallback);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private ScanCallback mCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
                Log.d("TAG", "Discover scan result");
                if (result != null){
                    ScanRecord scanRecord = result.getScanRecord();
                    assert scanRecord != null;
                    List<ParcelUuid> serviceUUIDs = scanRecord.getServiceUuids();
                    for (ParcelUuid uuid1 : serviceUUIDs){
                        if (uuid1.equals(uuid)){
                            byte[] data = scanRecord.getServiceData(uuid1);
                            String s = new String(data);
                            serviceUUID.setText(uuid1.toString());
                            serviceData.setText(s);
                        }
                    }
                }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e("TAG", "Scan failed" + errorCode);
        }
    };
}
