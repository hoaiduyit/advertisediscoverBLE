package com.hoaiduy.blebeacon.view;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hoaiduy.blebeacon.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by hoaiduy2503 on 8/22/2017.
 */

@RequiresApi(api = Build.VERSION_CODES.O)
public class BLEDeviceAdapter extends RecyclerView.Adapter<BLEDeviceAdapter.DeviceHolder> {

    private List<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    private Context mContext;
    private Dialog dialog;
    TextView txtAmountSend, btnNo, btnYes;

    public BLEDeviceAdapter(Context context, ArrayList<BluetoothDevice> devices){
        this.mDeviceList = devices;
        this.mContext = context;
        setupDialog();
    }

    private void setupDialog() {
        dialog = new Dialog(mContext);
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
        String decode = new String(byteArr);
        String[] sub = decode.split(":");
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
