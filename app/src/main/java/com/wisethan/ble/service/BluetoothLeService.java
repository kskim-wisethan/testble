package com.wisethan.ble.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.wisethan.ble.util.Constants;

import java.util.List;
import java.util.UUID;


public class BluetoothLeService extends Service {   // ble 값 받아오기
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private String broad;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";
    public final static String EXTRA_DATA_BYTES = "com.example.bluetooth.le.EXTRA_DATA_BYTES";

    public final static String ACTION_GATT_CONNECTED_WIDGET = "com.example.bluetooth.le.ACTION_GATT_CONNECTED_WIDGET";
    public final static String ACTION_GATT_DISCONNECTED_WIDGET = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED_WIDGET";
    public final static String ACTION_GATT_SERVICES_DISCOVERED_WIDGET = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED_WIDGET";
    public final static String ACTION_DATA_AVAILABLE_WIDGET = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE_WIDGET";
    public final static String EXTRA_DATA_WIDGET = "com.example.bluetooth.le.EXTRA_DATA_WIDGET";
    public final static String EXTRA_DATA_BYTES_WIDGET = "com.example.bluetooth.le.EXTRA_DATA_BYTES_WIDGET";

    public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString(Constants.HEART_RATE_MEASUREMENT);
    public final static UUID UUID_CUSTOM_CHARACTERISTIC1 = UUID.fromString(Constants.CUSTOM_CHARACTERISTIC1);
    public final static UUID UUID_CUSTOM_CHARACTERISTIC2 = UUID.fromString(Constants.CUSTOM_CHARACTERISTIC2);
    public final static UUID UUID_CUSTOM_CHARACTERISTIC3 = UUID.fromString(Constants.CUSTOM_CHARACTERISTIC3);
    public final static UUID UUID_CUSTOM_CHARACTERISTIC4 = UUID.fromString(Constants.CUSTOM_CHARACTERISTIC4);


    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {   //callback시 브로드캐스트 전송
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            SharedPreferences sharedPreferences = getSharedPreferences("Broadcast", Context.MODE_PRIVATE);
            broad = sharedPreferences.getString("broad", "");

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if(broad.equals("widget")){
                    intentAction = ACTION_GATT_CONNECTED_WIDGET;
                }else {
                    intentAction = ACTION_GATT_CONNECTED;
                }
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if(broad.equals("widget")){
                    intentAction = ACTION_GATT_DISCONNECTED_WIDGET;
                }else {
                    intentAction = ACTION_GATT_DISCONNECTED;
                }

                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            SharedPreferences sharedPreferences = getSharedPreferences("Broadcast", Context.MODE_PRIVATE);
            broad = sharedPreferences.getString("broad", "");

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if(broad.equals("widget")){
                    broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED_WIDGET);
                }else {
                    broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {

            SharedPreferences sharedPreferences = getSharedPreferences("Broadcast", Context.MODE_PRIVATE);
            broad = sharedPreferences.getString("broad", "");

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if(broad.equals("widget")){
                    broadcastUpdate(ACTION_DATA_AVAILABLE_WIDGET, characteristic);
                }else {
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                }

            }
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        SharedPreferences sharedPreferences = getSharedPreferences("Broadcast", Context.MODE_PRIVATE);
        broad = sharedPreferences.getString("broad", "");

        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));

        } else {
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data) {
                    stringBuilder.append(String.format("%02X ", byteChar));
                }
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());

                if(broad.equals("widget")){
                    intent.putExtra(EXTRA_DATA_BYTES_WIDGET, data);
                }else {
                    intent.putExtra(EXTRA_DATA_BYTES, data);
                }
            }
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    public boolean initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }

        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(Constants.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;
        return mBluetoothGatt.getServices();
    }
}
