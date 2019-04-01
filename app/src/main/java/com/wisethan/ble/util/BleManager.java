package com.wisethan.ble.util;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.wisethan.ble.R;
import com.wisethan.ble.model.BleModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BleManager {  //ble 연결
    private static final String TAG = BleManager.class.getSimpleName();

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_FINE_LOCATION = 2;
    private static final long SCAN_PERIOD = 10000;

    private BluetoothGatt mBluetoothGatt;





    boolean overlap = false;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    ArrayList<String> uuid = new ArrayList<String>();
    private boolean mScanning = false;
    private boolean mConnected = false;
    private String mDeviceAddress = "";
    private Handler mHandler = new Handler();

    private Activity mParent;
    private BluetoothAdapter mBluetoothAdapter;
    private BleDeviceCallback mDeviceCallback;


    private static BleManager mInstance;
    public static BleManager getInstance(Activity parent) {
        if (mInstance == null) {
            mInstance = new BleManager(parent);
        }
        return mInstance;
    }

    public interface BleDeviceCallback {
        public void onResponse(BleModel model);
    }

    private BleManager(Activity parent) {
        mParent = parent;
        checkBleEnable();
    }

    public void checkBleEnable() {   //블루투스 on/off 확인 후 off시 intent 보냄
        if (!mParent.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(mParent, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
        } else {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                Toast.makeText(mParent, R.string.bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            }
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                mParent.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

            }
        }
    }

    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {  //ble 검색 될 때마다 callback하여 ble정보를 BleModel에 저장하고 onResponse로 보내줌
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            mParent.runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    System.out.println("!!!!!!!!!!!!!!"+uuid.size()
                    );
                    if(uuid.size()==0){
                        uuid.add(device.getAddress());
                    }else {
                        for (int i = 0 ; i<uuid.size() ;i++) {
                            if(uuid.get(i).equals(device.getAddress())) {
                                overlap = true;
                                break;
                            }
                        }

                        if(overlap==false){

                            uuid.add(device.getAddress());
                            Map<String, Object> data = new HashMap<>();
                            data.put("uuid", device.getAddress());
                            data.put("rssi", String.valueOf(rssi));
                            data.put("name", (device.getName() == null) ? mParent.getString(R.string.unknown_device) : device.getName());
                            data.put("description", "");
                            data.put("scan_record", StringUtils.byteArrayInHexFormat(scanRecord));
                            BleModel ble = new BleModel(data);

                            mDeviceCallback.onResponse(ble);
                        }
                        overlap=false;
                    }
                }
            });
        }
    };

    public void scanBleDevice(final BleDeviceCallback callback) {  //ble 스캔
        System.out.println("scanBleDevice");
        mDeviceCallback = callback;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mScanning) {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(leScanCallback);
                }
            }
        }, SCAN_PERIOD);

        mScanning = true;
        mBluetoothAdapter.startLeScan(leScanCallback);
    }

    public void stopScan() {
        mScanning = false;
        mBluetoothAdapter.stopLeScan(leScanCallback);
    }
}
