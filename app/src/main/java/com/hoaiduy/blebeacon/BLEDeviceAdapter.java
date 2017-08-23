package com.hoaiduy.blebeacon;

import android.app.Activity;
import android.app.Dialog;
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
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by hoaiduy2503 on 8/22/2017.
 */

@RequiresApi(api = Build.VERSION_CODES.O)
public class BLEDeviceAdapter extends RecyclerView.Adapter<BLEDeviceAdapter.DeviceHolder> {

    private List<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    private List<ScanFilter> filters = new ArrayList<ScanFilter>();
    private ParcelUuid uuid;
    private BluetoothLeScanner mBluetoothLeScanner;
    private Handler mHandler;
    private Activity activity;
    private ScanCallback mCallback;
    private ScanRecord scanRecord;
    private Dialog dialog;

    public BLEDeviceAdapter(ArrayList<BluetoothDevice> devices, Activity activity){
        this.mDeviceList = devices;
        this.activity = activity;

        uuid = new ParcelUuid(UUID.fromString("00001011-0000-1000-8000-00805f9b34fb"));
        mHandler = new Handler();

        final BluetoothManager mBluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        if (mDeviceList != null){
            mDeviceList.clear();
            Scan(true);
        }else {
            Scan(true);
        }

        setupDialog();
    }

    private void setupDialog() {
        dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_discover);

    }

    @Override
    public DeviceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_view, parent, false);
        return new DeviceHolder(view);
    }

    @Override
    public void onBindViewHolder(DeviceHolder holder, int position) {
        assert scanRecord != null;
        List<ParcelUuid> serviceUUIDs = scanRecord.getServiceUuids();
        for (ParcelUuid uuid1 : serviceUUIDs){
            if (uuid1.equals(uuid)){
                String dataName = scanRecord.getDeviceName();
                assert dataName != null;
                byte[] convert = dataName.getBytes();
                byte[] byteArr = Base64.decode(convert, Base64.DEFAULT);
                String encode = new String(byteArr);
                String[] sub = encode.split(":");
                String indicator = sub[0];
                String amount = sub[1];
                if (indicator.equalsIgnoreCase("mp")){
                    holder.serviceData.setText("Amount: " + amount);
                }
            }
        }
        holder.serviceData.setOnClickListener(view -> {
            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return mDeviceList.size();
    }

    private void Scan(boolean enable){

        mCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                Log.d("TAG", "Discover scan result");
                if (result != null){
                    assert scanRecord != null;
                    scanRecord = result.getScanRecord();
                    BluetoothDevice device = result.getDevice();
                    Log.d("TAG", scanRecord.toString());
                    List<ParcelUuid> serviceUUIDs = scanRecord.getServiceUuids();
                    for (ParcelUuid uuid1 : serviceUUIDs){
                        if (uuid1.equals(uuid)){
                            String dataName = scanRecord.getDeviceName();
                            assert dataName != null;
                            byte[] convert = dataName.getBytes();
                            byte[] byteArr = Base64.decode(convert, Base64.DEFAULT);
                            String encode = new String(byteArr);
                            String[] sub = encode.split(":");
                            String indicator = sub[0];
                            if (indicator.equalsIgnoreCase("mp")){
                                mDeviceList.add(device);
                                notifyDataSetChanged();
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
                Log.e("TAG", "Scan failed" + errorCode);
            }
        };

        ScanFilter filter = new ScanFilter.Builder()
                .setServiceUuid(uuid)
                .build();
        filters.add(filter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build();
        try {
            if (enable){
                mHandler.postDelayed(() -> mBluetoothLeScanner.stopScan(mCallback), 3000);
                mBluetoothLeScanner.startScan(filters, settings, mCallback);
            }else {
                mBluetoothLeScanner.stopScan(mCallback);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    class DeviceHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.serviceData)
        TextView serviceData;

        public DeviceHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
