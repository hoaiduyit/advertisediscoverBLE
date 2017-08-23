package com.hoaiduy.blebeacon.presenter;

import android.bluetooth.le.ScanFilter;
import android.content.Context;
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

    private Context mContext;
    private ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>();

    public DiscoverPresenter(Context context) {
        this.mContext = context;
    }

    public void setupRecycleView(RecyclerView recyclerView){
        BLEDeviceAdapter adapter = new BLEDeviceAdapter(filters);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
