package com.hoaiduy.blebeacon.view;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hoaiduy.blebeacon.R;
import com.hoaiduy.blebeacon.advertise.AdvertiseBLE;
import com.hoaiduy.blebeacon.discover.DiscoverBLE;
import com.hoaiduy.blebeacon.utils.DialogUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {

    private AdvertiseBLE advertiseBLE;
    private DiscoverBLE discoverBLE;
    private BLEDeviceAdapter adapter;
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int MY_BLUETOOTH_ENABLE_REQUEST_ID = 6;

    @BindView(R.id.btnAdvertise)
    Button btnAdvertise;
    @BindView(R.id.btnDiscover)
    Button btnDiscover;
    @BindView(R.id.ll_recycle)
    LinearLayout ll_recycle;
    @BindView(R.id.ll_adv)
    LinearLayout ll_adv;
    @BindView(R.id.txtState)
    TextView txtState;
    @BindView(R.id.txtAmount)
    TextView txtViewAmount;
    @BindView(R.id.recycleView)
    RecyclerView recyclerView;

    RecyclerView.LayoutManager layoutManager;

    private Dialog dialog;
    Button btnRequest;
    TextView txtAmount, txtTitle;
    private ProgressDialog progressDialog;
    private Dialog dialogItem;
    TextView txtAmountSend, btnNo, btnYes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();

        if (!this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            btnAdvertise.setEnabled(false);
            btnDiscover.setEnabled(false);
        }

        if (!mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, MY_BLUETOOTH_ENABLE_REQUEST_ID);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(dialog -> requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION));
                builder.show();
            }
        }

        advertiseBLE = new AdvertiseBLE(this);
        discoverBLE = new DiscoverBLE(this, mDeviceList);

        setupUI();
        setupDiscoverUI();
        setupDialog();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("TAG", "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(dialog -> {
                    });
                    builder.show();
                }
            }
        }
    }

    private void setupUI() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_advertise);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnRequest = dialog.findViewById(R.id.btnRequest);
        txtAmount = dialog.findViewById(R.id.txtAmount);
        txtTitle = dialog.findViewById(R.id.txtTitle);
        txtTitle.setText(getString(R.string.request_money));

        addControls();
    }

    private void addControls() {
        btnAdvertise.setOnClickListener(view -> {
            if (advertiseBLE.isAdvertising()){
                advertiseBLE.stopAdvertise();
                ll_adv.setVisibility(View.GONE);
                dialog.show();
            } else {
                dialog.show();
            }
        });

        btnDiscover.setOnClickListener(view -> {
            progressDialog = DialogUtils.getLoadingProgressDialog(this);
            if (mDeviceList.isEmpty()){
                if (advertiseBLE.isAdvertising()){
                    isAdvertising();
                }else {
                    notAdvertising();
                }
            } else {
                mDeviceList.clear();
                if (advertiseBLE.isAdvertising()){
                    isAdvertising();
                }else {
                    notAdvertising();
                }
            }
        });

        btnRequest.setOnClickListener(view -> {
            if (ll_recycle.getVisibility() == View.VISIBLE){
                String amount = txtAmount.getText().toString();
                ll_adv.setVisibility(View.VISIBLE);
                ll_recycle.setVisibility(View.GONE);
                dialog.dismiss();
                advertiseBLE.startAdvertise(amount,
                        getString(R.string.uuid),
                        txtState,
                        txtViewAmount);
            } else {
                String amount = txtAmount.getText().toString();
                ll_adv.setVisibility(View.VISIBLE);
                dialog.dismiss();
                advertiseBLE.startAdvertise(amount,
                        getString(R.string.uuid),
                        txtState,
                        txtViewAmount);
            }
        });
    }

    private void notAdvertising() {
        discoverBLE.startScan(getString(R.string.uuid),
                dialogItem,
                txtAmountSend,
                progressDialog,
                adapter);
        ll_recycle.setVisibility(View.VISIBLE);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void isAdvertising() {
        advertiseBLE.stopAdvertise();
        discoverBLE.startScan(getString(R.string.uuid),
                dialogItem,
                txtAmountSend,
                progressDialog,
                adapter);
        ll_adv.setVisibility(View.GONE);
        ll_recycle.setVisibility(View.VISIBLE);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void setupDiscoverUI() {
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new BLEDeviceAdapter(this, mDeviceList);
    }

    private void setupDialog() {
        dialogItem = new Dialog(this);
        dialogItem.setContentView(R.layout.dialog_discover);
        dialogItem.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        txtAmountSend = dialogItem.findViewById(R.id.txtAmountSend);
        btnNo = dialogItem.findViewById(R.id.btnNo);
        btnYes = dialogItem.findViewById(R.id.btnYes);

        btnNo.setOnClickListener(view -> dialogItem.dismiss());
    }
}
