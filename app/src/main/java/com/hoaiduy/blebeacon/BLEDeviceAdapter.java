package com.hoaiduy.blebeacon;

import android.app.Activity;
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
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hoaiduy.blebeacon.utils.DialogUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
    TextView txtAmountSend, btnNo, btnYes;
    public ProgressDialog progressDialog;

    public BLEDeviceAdapter(ArrayList<BluetoothDevice> devices, Activity activity){
        this.mDeviceList = devices;
        this.activity = activity;

        uuid = new ParcelUuid(UUID.fromString(activity.getString(R.string.uuid)));
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

        progressDialog = DialogUtils.getLoadingProgressDialog(activity);

        setupDialog();
    }

    private void setupDialog() {
        dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_discover);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        txtAmountSend = dialog.findViewById(R.id.txtAmountSend);
        btnNo = dialog.findViewById(R.id.btnNo);
        btnYes = dialog.findViewById(R.id.btnYes);

        btnNo.setOnClickListener(view -> dialog.dismiss());
    }

    @Override
    public DeviceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_view, parent, false);
        return new DeviceHolder(view);
    }

    @Override
    public void onBindViewHolder(DeviceHolder holder, int position) {
        BluetoothDevice bluetoothDevice = mDeviceList.get(position);
        String dataName = bluetoothDevice.getName();
        assert dataName != null;
        byte[] convert = dataName.getBytes();
        byte[] byteArr = Base64.decode(convert, Base64.DEFAULT);
        String encode = new String(byteArr);
        String[] sub = encode.split(":");
        String amount = sub[1];
        holder.serviceData.setText("Amount: " + amount);
        holder.ll_item.setOnClickListener(view -> {
            txtAmountSend.setText("Do you want to send " + amount);
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
                if (result != null){
                    scanRecord = result.getScanRecord();
                    BluetoothDevice device = result.getDevice();
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
                                mDeviceList.add(device);
                                removeDuplicateWithOrder(mDeviceList);
                                notifyDataSetChanged();
                                if (mDeviceList.size() != 0){
                                    if (mDeviceList.size() < 2){
                                        txtAmountSend.setText("Do you want to send " + amount);
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

        ScanFilter filter = new ScanFilter.Builder()
                .setServiceUuid(uuid)
                .build();
        filters.add(filter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build();
        try {
            if (enable){
                mHandler.postDelayed(() -> {
                    mBluetoothLeScanner.stopScan(mCallback);
                    progressDialog.show();
                    if (mDeviceList.size() != 0){
                        if (mDeviceList.size() < 2){
                            progressDialog.dismiss();
                            dialog.show();
                        } else {
                            progressDialog.dismiss();
                        }
                    } else {
                        progressDialog.dismiss();
                    }
                }, 3000);
                mBluetoothLeScanner.startScan(filters, settings, mCallback);
            }else {
                progressDialog.dismiss();
                mBluetoothLeScanner.stopScan(mCallback);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private static List<BluetoothDevice> removeDuplicateWithOrder(List<BluetoothDevice> arrList)
    {
        Set<Object> set = new HashSet<>();
        List newList = new ArrayList();
        for (Iterator<BluetoothDevice> iter = arrList.iterator(); iter.hasNext();) {
            Object element = iter.next();
            if (set.add(element))
                newList.add(element);
        }
        arrList.clear();
        arrList.addAll(newList);

        return arrList;
    }

    class DeviceHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.serviceData)
        TextView serviceData;
        @BindView(R.id.ll_item)
        LinearLayout ll_item;

        public DeviceHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
