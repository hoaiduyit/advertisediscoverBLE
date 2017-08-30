package com.hoaiduy.blebeacon.advertise;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Intent;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.hoaiduy.blebeacon.R;

import java.util.UUID;

/**
 * Created by hoaiduy2503 on 8/28/2017.
 */

@RequiresApi(api = Build.VERSION_CODES.O)
public class AdvertiseBLE {

    private Activity mActivity;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser advertiser;
    private AdvertiseCallback advertiseCallback;
    private static final int MY_BLUETOOTH_ENABLE_REQUEST_ID = 6;

    public AdvertiseBLE(Activity activity){
        this.mActivity = activity;
        BluetoothManager mBluetoothManager = (BluetoothManager) activity.getSystemService(Activity.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if (!mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, MY_BLUETOOTH_ENABLE_REQUEST_ID);
        }
    }

    //check state is advertising or not
    public boolean isAdvertising(){
        return advertiser != null;
    }

    // start advertising
    //textState and textAmount use to know BLE is advertise or not advertise
    public void startAdvertise(String dataBroadcast, String serviceUUID) {

         advertiseCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                Log.w("TAG", "GATT service ready");
            }

            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);
                switch (errorCode) {
                    case AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED:
                        Log.e("TAG", mActivity.getString(R.string.already_started));
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE:
                        Log.e("TAG", mActivity.getString(R.string.data_too_large));
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                        Log.e("TAG", mActivity.getString(R.string.feature_unsupported));
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR:
                        Log.e("TAG", mActivity.getString(R.string.internal_error));
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                        Log.e("TAG", mActivity.getString(R.string.too_many_advertisers));
                        break;
                }
            }
        };

        mBluetoothAdapter.setName(dataBroadcast);

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                .setConnectable(true)
                .build();

        ParcelUuid uuid = new ParcelUuid(UUID.fromString(serviceUUID));

        AdvertiseData data = new AdvertiseData.Builder()
                .addServiceUuid(uuid)
                .setIncludeDeviceName(true)
                .build();
        advertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        advertiser.startAdvertising(settings, data, advertiseCallback);
    }

    //stop advertising
    public void stopAdvertise(){
        advertiser.stopAdvertising(advertiseCallback);
    }
}
