package com.hoaiduy.blebeacon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
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

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BLEDeviceAdapter extends RecyclerView.Adapter<BLEDeviceAdapter.DeviceHolder> {

    private List<ScanFilter> mDeviceList = new ArrayList<ScanFilter>();
    private ParcelUuid uuid;
    private BluetoothLeScanner mBluetoothLeScanner;
    private Handler mHandler;

    public BLEDeviceAdapter(ArrayList<ScanFilter> devices){
        this.mDeviceList = devices;
    }

    @Override
    public DeviceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_view, parent, false);
        uuid = new ParcelUuid(UUID.fromString("3802EC45-B8FA-431B-ADCB-B806069F6168"));
        mHandler = new Handler();
        mBluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
        return new DeviceHolder(view);
    }

    @Override
    public void onBindViewHolder(DeviceHolder holder, int position) {

        ScanCallback mCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                Log.d("TAG", "Discover scan result");
                if (result != null){
                    ScanRecord scanRecord = result.getScanRecord();
                    assert scanRecord != null;
                    List<ParcelUuid> serviceUUID = scanRecord.getServiceUuids();
                    for (ParcelUuid uuid1 : serviceUUID){
                        if (uuid1.equals(uuid)){
                            byte[] data = scanRecord.getServiceData(uuid1);
                            String s = new String(data);
                            holder.serviceUUID.setText(uuid1.toString());
                            holder.serviceData.setText(s);
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
        mDeviceList.add(filter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build();

        mBluetoothLeScanner.startScan(mDeviceList, settings, mCallback);

        mHandler.postDelayed(() -> mBluetoothLeScanner.stopScan(mCallback), 3000);
    }

    @Override
    public int getItemCount() {
        return mDeviceList.size();
    }


    class DeviceHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.serviceUUID)
        TextView serviceUUID;
        @BindView(R.id.serviceData)
        TextView serviceData;
        public DeviceHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
