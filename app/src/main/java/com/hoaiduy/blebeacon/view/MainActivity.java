package com.hoaiduy.blebeacon.view;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hoaiduy.blebeacon.R;
import com.hoaiduy.blebeacon.advertise.AdvertiseBLE;
import com.hoaiduy.blebeacon.discover.DiscoverBLE;
import com.hoaiduy.blebeacon.receiverinterface.BluetoothLeListener;
import com.hoaiduy.blebeacon.receiverinterface.Receiver;
import com.hoaiduy.blebeacon.utils.DialogUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {

    private AdvertiseBLE advertiseBLE;
    private DiscoverBLE discoverBLE;
    private BLEDeviceAdapter adapter;
    private Receiver mReceiver;
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    private ArrayList<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    @BindView(R.id.btnAdvertise)
    Button btnAdvertise;
    @BindView(R.id.btnDiscover)
    Button btnDiscover;
    @BindView(R.id.ll_recycle)
    LinearLayout ll_recycle;
    @BindView(R.id.ll_adv)
    LinearLayout ll_adv;
    @BindView(R.id.txtState)
    TextView txtState;
    @BindView(R.id.txtAmount)
    TextView txtViewAmount;
    @BindView(R.id.recycleView)
    RecyclerView recyclerView;

    RecyclerView.LayoutManager layoutManager;

    private Dialog dialog;
    Button btnRequest;
    TextView txtAmount, txtTitle;
    private ProgressDialog progressDialog;
    private Dialog dialogItem;
    TextView txtAmountSend, btnNo, btnYes;
    private Handler mHandler;
    private int scanTime = 3000;
    private String amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mHandler = new Handler();

        if (!this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            btnAdvertise.setEnabled(false);
            btnDiscover.setEnabled(false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(dialog -> requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION));
                builder.show();
            }
        }

        advertiseBLE = new AdvertiseBLE(this);
        discoverBLE = new DiscoverBLE(this, mDeviceList);
        mReceiver = new Receiver();

        setupUI();
        setupDiscoverUI();
        setupDialog();
        setupBroadcastReceiver();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("TAG", "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(dialog -> {
                    });
                    builder.show();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    private void setupUI() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_advertise);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnRequest = dialog.findViewById(R.id.btnRequest);
        txtAmount = dialog.findViewById(R.id.txtAmount);
        txtTitle = dialog.findViewById(R.id.txtTitle);
        txtTitle.setText(getString(R.string.request_money));

        addControls();
    }

    private void addControls() {
        btnAdvertise.setOnClickListener(view -> {
            if (advertiseBLE.isAdvertising()){
                advertiseBLE.stopAdvertise();
                ll_adv.setVisibility(View.GONE);
                dialog.show();
            } else {
                dialog.show();
            }
        });

        btnDiscover.setOnClickListener(view -> {
            progressDialog = DialogUtils.getLoadingProgressDialog(this);
            if (mDeviceList.isEmpty()){
                if (advertiseBLE.isAdvertising()){
                    isAdvertising();
                }else {
                    notAdvertising();
                }
            } else {
                mDeviceList.clear();
                if (advertiseBLE.isAdvertising()){
                    isAdvertising();
                }else {
                    notAdvertising();
                }
            }
        });

        btnRequest.setOnClickListener(view -> {
            amount = "mp:" + txtAmount.getText().toString();
            byte[] byteArr = amount.getBytes();
            byte[] encode = Base64.encode(byteArr, Base64.DEFAULT);
            String stringByte = new String(encode);

            ll_adv.setVisibility(View.VISIBLE);
            ll_recycle.setVisibility(View.GONE);
            dialog.dismiss();
            advertiseBLE.startAdvertise(stringByte, getString(R.string.uuid));
        });
    }

    private void notAdvertising() {
        discoverBLE.startScan(getString(R.string.uuid));
        mHandler.postDelayed(() -> {
            discoverBLE.stopScan();
            progressDialog.show();{
                if (!mDeviceList.isEmpty()){
                    if (mDeviceList.size() < 2){
                        progressDialog.dismiss();
                        txtAmountSend.setText("Do you want to send this money?");
                        dialog.show();
                    }else {
                        progressDialog.dismiss();
                    }
                }else {
                    progressDialog.dismiss();
                }
            }
        }, scanTime);
        ll_recycle.setVisibility(View.VISIBLE);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void isAdvertising() {
        advertiseBLE.stopAdvertise();
        discoverBLE.startScan(getString(R.string.uuid));
        mHandler.postDelayed(() -> {
            discoverBLE.stopScan();
            progressDialog.show();{
                if (!mDeviceList.isEmpty()){
                    if (mDeviceList.size() < 2){
                        progressDialog.dismiss();
                        txtAmountSend.setText("Do you want to send this money?");
                        dialog.show();
                    }else {
                        progressDialog.dismiss();
                    }
                }else {
                    progressDialog.dismiss();
                }
            }
        }, scanTime);
        ll_adv.setVisibility(View.GONE);
        ll_recycle.setVisibility(View.VISIBLE);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void setupDiscoverUI() {
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new BLEDeviceAdapter(this, mDeviceList);
    }

    private void setupDialog() {
        dialogItem = new Dialog(this);
        dialogItem.setContentView(R.layout.dialog_discover);
        dialogItem.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        txtAmountSend = dialogItem.findViewById(R.id.txtAmountSend);
        btnNo = dialogItem.findViewById(R.id.btnNo);
        btnYes = dialogItem.findViewById(R.id.btnYes);

        btnNo.setOnClickListener(view -> dialogItem.dismiss());
    }

    private void setupBroadcastReceiver() {
        mReceiver.setOnBluetoothLeListener(new BluetoothLeListener() {
            @Override
            public void onAdvertiseSuccess(String action) {
                if (AdvertiseBLE.ACTION_ADVERTISE_SUCCESS.equals(action)){
                    txtState.setText("Advertising...");
                    txtViewAmount.setText(amount);
                }
            }

            @Override
            public void onAdvertiseFail(String action) {
                if (AdvertiseBLE.ACTION_ADVERTISE_FAIL.equals(action)){
                    txtState.setText("Advertise fail");
                    txtViewAmount.setText("");
                }
            }

            @Override
            public void onDiscoverSuccess(String action) {
                if (DiscoverBLE.ACTION_DISCOVER_SUCCESS.equals(action)){
                    for (BluetoothDevice device : mDeviceList){
                        deviceList.add(device);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onDiscoverFail(String action) {
                if (DiscoverBLE.ACTION_DISCOVER_FAIL.equals(action)){
                    return;
                }
            }
        });
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AdvertiseBLE.ACTION_ADVERTISE_SUCCESS);
        intentFilter.addAction(AdvertiseBLE.ACTION_ADVERTISE_FAIL);
        return intentFilter;
    }
}
