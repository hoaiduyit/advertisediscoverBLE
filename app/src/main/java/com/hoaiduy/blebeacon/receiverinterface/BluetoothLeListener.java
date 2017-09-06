package com.hoaiduy.blebeacon.receiverinterface;

/**
 * Created by hoaiduy2503 on 9/6/2017.
 */

public interface BluetoothLeListener {
    void onAdvertiseSuccess(String action);
    void onAdvertiseFail(String action);
    void onDiscoverSuccess(String action);
    void onDiscoverFail(String action);
}
