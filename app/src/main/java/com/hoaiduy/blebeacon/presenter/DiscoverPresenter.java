package com.hoaiduy.blebeacon.presenter;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;

import com.hoaiduy.blebeacon.BLEDeviceAdapter;

import java.util.ArrayList;

/**
 * Created by hoaiduy2503 on 8/22/2017.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class DiscoverPresenter {

    private Activity activity;
    private ArrayList<BluetoothDevice> mDevicelist = new ArrayList<BluetoothDevice>();

    public DiscoverPresenter(Activity activity) {
        this.activity = activity;
    }

    public void setupRecycleView(RecyclerView recyclerView){
        BLEDeviceAdapter adapter = new BLEDeviceAdapter(mDevicelist, activity);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
