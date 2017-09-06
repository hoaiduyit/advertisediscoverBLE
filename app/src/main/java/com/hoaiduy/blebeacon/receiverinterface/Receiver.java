package com.hoaiduy.blebeacon.receiverinterface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by hoaiduy2503 on 9/6/2017.
 */

public class Receiver extends BroadcastReceiver {
    BluetoothLeListener leListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        leListener.onAdvertiseSuccess(action);
        leListener.onAdvertiseFail(action);
        leListener.onDiscoverSuccess(action);
        leListener.onDiscoverFail(action);
    }

    public void setOnBluetoothLeListener(BluetoothLeListener bluetoothLeListener){
        this.leListener = bluetoothLeListener;
    }
}
