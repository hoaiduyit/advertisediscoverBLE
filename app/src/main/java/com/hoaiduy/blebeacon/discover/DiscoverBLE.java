package com.hoaiduy.blebeacon.discover;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

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

    private Context mContext;
    private BluetoothLeScanner mBluetoothLeScanner;
    private List<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    private Handler mHandler;
    private ScanCallback mCallback;
    private int scanTime = 3000;

    public DiscoverBLE(Context context, ArrayList<BluetoothDevice> listDevices){
        this.mContext = context;
        this.mDeviceList = listDevices;

        mHandler = new Handler();

        final BluetoothManager mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    //dialog1Device will show if BLE scanned and saw 1 device in the list
    //textAmountDialog show text message in dialog1Device
    //progressDialog will spin before scan complete
    //adapter will change data for every single item was added in the list. We can use another custom adapter to replace this.
    public void startScan(String serviceUUID,
                          Dialog dialog1Device,
                          TextView textAmountDialog,
                          ProgressDialog progressDialog,
                          BLEDeviceAdapter adapter){

        ParcelUuid uuid = new ParcelUuid(UUID.fromString(serviceUUID));

        mCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                if (result != null){
                    ScanRecord scanRecord = result.getScanRecord();
                    BluetoothDevice device = result.getDevice();
                    List<ParcelUuid> serviceUUIDs = scanRecord.getServiceUuids();
                    for (ParcelUuid uuid1 : serviceUUIDs){
                        if (uuid1.equals(uuid)){
                            String dataName = scanRecord.getDeviceName();
                            assert dataName != null;
                            byte[] convert = dataName.getBytes();
                            byte[] byteArr = Base64.decode(convert, Base64.DEFAULT);
                            String decode = new String(byteArr);
                            String[] sub = decode.split(":");
                            String indicator = sub[0];
                            String amount = sub[1];
                            if (indicator.equalsIgnoreCase("mp")){
                                mDeviceList.add(device);
                                removeDuplicateWithOrder(mDeviceList);
                                adapter.notifyDataSetChanged();

                                //immediately show text if after scan list has 1 device
                                if (mDeviceList.size() != 0){
                                    if (mDeviceList.size() < 2){
                                        textAmountDialog.setText("Do you want to send " + amount);
                                    }
                                }
                            }
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
        try {

            //stop scanning after 3s
            mHandler.postDelayed(() -> {
                mBluetoothLeScanner.stopScan(mCallback);
                progressDialog.show();
                if (mDeviceList.size() != 0){
                    if (mDeviceList.size() < 2){
                        progressDialog.dismiss();
                        dialog1Device.show();
                    } else {
                        progressDialog.dismiss();
                    }
                } else {
                    progressDialog.dismiss();
                }
            }, scanTime);
            mBluetoothLeScanner.startScan(filters, settings, mCallback);
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
