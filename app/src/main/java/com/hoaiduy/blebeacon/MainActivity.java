package com.hoaiduy.blebeacon;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btnAdvertise)
    Button btnAdvertise;
    @BindView(R.id.btnDiscover)
    Button btnDiscover;
    @BindView(R.id.txtUuid)
    TextView txtUuid;
    @BindView(R.id.txtIndicator)
    TextView txtIndicator;
    @BindView(R.id.txtAmount)
    TextView txtViewAmount;
    @BindView(R.id.ll_adv)
    LinearLayout ll_adv;

    Button btnRequest;
    EditText txtAmount;
    TextView txtTitle;
    BluetoothLeAdvertiser advertiser;
    private String indicator;
    private String amountData;
    private Dialog dialog;
    private BluetoothAdapter mBluetoothAdapter;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        advertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();

        if (!this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            btnAdvertise.setEnabled(false);
            btnDiscover.setEnabled(false);
        }

        setupUI();

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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
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
                return;
            }
        }
    }

    private void setupUI(){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_advertise);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnRequest = dialog.findViewById(R.id.btnRequest);
        txtAmount = dialog.findViewById(R.id.txtAmount);
        txtTitle = dialog.findViewById(R.id.txtTitle);
        txtTitle.setText(getString(R.string.request_money));

        btnAdvertise.setOnClickListener(view -> {
            if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON){
                advertiser.stopAdvertising(advertiseCallback);
                ll_adv.setVisibility(View.GONE);
                dialog.show();
            } else {
                dialog.show();
            }
        });
        btnDiscover.setOnClickListener(view -> {
            if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON){
                advertiser.stopAdvertising(advertiseCallback);
                ll_adv.setVisibility(View.GONE);
                Intent intent = new Intent(this, BLEDiscoveryActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, BLEDiscoveryActivity.class);
                startActivity(intent);
            }
        });

        btnRequest.setOnClickListener(view -> {
            ll_adv.setVisibility(View.VISIBLE);
            dialog.dismiss();
            advertise(txtAmount.getText().toString());
        });
    }

    private void advertise(String text) {
        indicator = "mp:";
        amountData = text;

        String dataName = indicator + amountData;

        byte[] byteArr = dataName.getBytes();
        byte[] decode = Base64.encode(byteArr, Base64.DEFAULT);

        String stringByte = new String(decode);

        mBluetoothAdapter.setName(stringByte);

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                .setConnectable(true)
                .build();

        ParcelUuid uuid = new ParcelUuid(UUID.fromString(getString(R.string.uuid)));

        AdvertiseData data = new AdvertiseData.Builder()
                .addServiceUuid(uuid)
                .setIncludeDeviceName(true)
                .build();

        advertiser.startAdvertising(settings, data, advertiseCallback);
    }

    private AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            String[] split = indicator.split(":");
            String indicatorMypay = split[0];

            txtUuid.setText("Advertising..");
            txtIndicator.setText("Indicator: " + indicatorMypay);
            txtViewAmount.setText("Amount: " + amountData);
            Log.w("TAG", "GATT service ready");
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            txtUuid.setText("Advertise failed");
            Log.e("TAG", "GATT Server Error " + errorCode);
        }
    };
}
