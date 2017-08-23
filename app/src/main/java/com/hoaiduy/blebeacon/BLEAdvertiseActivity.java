package com.hoaiduy.blebeacon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.math.BigInteger;
import java.util.UUID;

/**
 * Created by hoaiduy2503 on 8/22/2017.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BLEAdvertiseActivity extends AppCompatActivity {

    TextView txtAdv, txtData;
    BluetoothLeAdvertiser advertiser;
    private String serviceData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertise);

        txtAdv = (TextView) findViewById(R.id.txtAdv);
        txtData = (TextView) findViewById(R.id.txtData);
        advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();

        Intent intent = getIntent();
        intent.getAction();

        if (!this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            txtAdv.setText(getString(R.string.not_support_BLE_text));
        }else {
            advertise();
        }
    }

    private void advertise() {
        serviceData = "mypay";
        byte[] theByteArray = serviceData.getBytes();

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                .setConnectable(true)
                .build();

        ParcelUuid uuid = new ParcelUuid(UUID.fromString(getString(R.string.uuid)));

        AdvertiseData data = new AdvertiseData.Builder()
                .addServiceUuid(uuid)
                .addServiceData(uuid, theByteArray)
                .build();

        advertiser.startAdvertising(settings, data, advertiseCallback);
    }

    private AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            txtAdv.setText("Advertised from: \n" + getString(R.string.uuid));
            txtData.setText("Data: " + serviceData);
            Log.w("TAG", "GATT service ready");
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.e("TAG", "GATT Server Error " + errorCode);
        }
    };

    private String toHex(String text){
        return String.format("%x", new BigInteger(1, text.getBytes()));
    }

    @Override
    public void onBackPressed() {
        advertiser.stopAdvertising(advertiseCallback);
        finish();
    }
}
