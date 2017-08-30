package com.hoaiduy.blebeacon.advertise;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import com.hoaiduy.blebeacon.R;

import java.util.UUID;

/**
 * Created by hoaiduy2503 on 8/28/2017.
 */

@RequiresApi(api = Build.VERSION_CODES.O)
public class AdvertiseBLE {

    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser advertiser;
    private AdvertiseCallback advertiseCallback;

    public AdvertiseBLE(Context context){
        this.mContext = context;
        BluetoothManager mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
    }

    //check state is advertising or not
    public boolean isAdvertising(){
        return advertiser != null;
    }

    // start advertising
    //textState and textAmount use to know BLE is advertise or not advertise
    public void startAdvertise(String amount, String serviceUUID, TextView textState, TextView textAmount) {

         advertiseCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                textState.setText("Advertising...");
                textAmount.setText(amount);
                Log.w("TAG", "GATT service ready");
            }

            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);
                String errorText = null;
                textAmount.setText("");
                switch (errorCode) {
                    case AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED:
                        errorText = mContext.getString(R.string.already_started);
                        Log.e("TAG", mContext.getString(R.string.already_started));
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE:
                        errorText = mContext.getString(R.string.data_too_large);
                        Log.e("TAG", mContext.getString(R.string.data_too_large));
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                        errorText = mContext.getString(R.string.feature_unsupported);
                        Log.e("TAG", mContext.getString(R.string.feature_unsupported));
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR:
                        errorText = mContext.getString(R.string.internal_error);
                        Log.e("TAG", mContext.getString(R.string.internal_error));
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                        errorText = mContext.getString(R.string.too_many_advertisers);
                        Log.e("TAG", mContext.getString(R.string.too_many_advertisers));
                        break;
                }
                textState.setText(errorText);
            }
        };

        String dataName = "mp:" + amount;

        byte[] byteArr = dataName.getBytes();
        byte[] encode = Base64.encode(byteArr, Base64.DEFAULT);

        String stringByte = new String(encode);

        mBluetoothAdapter.setName(stringByte);

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
