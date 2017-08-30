package com.hoaiduy.blebeacon.discover;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.hoaiduy.blebeacon.view.BLEDeviceAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by hoaiduy2503 on 8/28/2017.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class DiscoverBLE {

    private Activity mActivity;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothAdapter mBluetoothAdapter;
    private List<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    private ScanCallback mCallback;

    public DiscoverBLE(Activity activity, ArrayList<BluetoothDevice> listDevices){
        this.mActivity = activity;
        this.mDeviceList = listDevices;
        final BluetoothManager mBluetoothManager = (BluetoothManager) activity.getSystemService(Activity.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
    }

    //adapter will change data for every single item was added in the list. We can use another custom adapter to replace this.
    public void startScan(String serviceUUID, BLEDeviceAdapter adapter){

        ParcelUuid uuid = new ParcelUuid(UUID.fromString(serviceUUID));

        mCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                if (result != null){
                    BluetoothDevice device = result.getDevice();
                    mDeviceList.add(device);
                    removeDuplicateWithOrder(mDeviceList);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.e("TAG", "Scan failed " + errorCode);
            }
        };

        List<ScanFilter> filters = new ArrayList<ScanFilter>();

        ScanFilter filter = new ScanFilter.Builder()
                .setServiceUuid(uuid)
                .build();
        filters.add(filter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build();

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        try {
            mBluetoothLeScanner.startScan(filters, settings, mCallback);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void stopScan(){
        try {
            mBluetoothLeScanner.stopScan(mCallback);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static List<BluetoothDevice> removeDuplicateWithOrder(List<BluetoothDevice> deviceList)
    {
        Set<Object> set = new HashSet<>();
        List newList = new ArrayList();
        for (BluetoothDevice device : deviceList) {
            if (set.add(device))
                newList.add(device);
        }
        deviceList.clear();
        deviceList.addAll(newList);

        return deviceList;
    }

}
