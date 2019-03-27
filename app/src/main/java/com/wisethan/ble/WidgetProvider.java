package com.wisethan.ble;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.wisethan.ble.service.BluetoothLeService;
import com.wisethan.ble.util.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class WidgetProvider extends AppWidgetProvider {

    String uuid;
    private int mServiceIndex = 0;
    private int mCharacteristicIndex = 0;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        SharedPreferences sharedPreferences = context.getSharedPreferences("SHARE_PREF",Context.MODE_PRIVATE);
        uuid = sharedPreferences.getString("uuid","!");
        AppWidgetManager app = AppWidgetManager.getInstance(context);
        int ids[] = app.getAppWidgetIds(new ComponentName(context, this.getClass()));
        for (int i = 0; i < ids.length; i++) {
            updateAppWidget(context, app, ids[i]);
        }

        mServiceIndex = 0;
        mCharacteristicIndex = 0;
//        requestCharacteristicValue();


    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));

        for (int i = 0; i < appWidgetIds.length; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager,int appWidgetId) {


        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
//        views.setTextViewText(R.id.temp_widget_tv, uuid);
//        Toast.makeText(context, "!!!"+uuid, Toast.LENGTH_SHORT).show();

        appWidgetManager.updateAppWidget(appWidgetId,views);



    }

//    void requestCharacteristicValue() {
//        if (mGattCharacteristics != null) {
//            final BluetoothGattCharacteristic characteristic = mGattCharacteristics.get(mServiceIndex).get(mCharacteristicIndex);
//            final int charaProp = characteristic.getProperties();
//            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
//                if (mNotifyCharacteristic != null) {
//                    mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
//                    mNotifyCharacteristic = null;
//                }
//                mBluetoothLeService.readCharacteristic(characteristic);
//            }
//            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
//                mNotifyCharacteristic = characteristic;
//                mBluetoothLeService.setCharacteristicNotification(characteristic, true);
//            }
//        }
//    }
//
//
//    private void displayGattServices(List<BluetoothGattService> gattServices) {
//        if (gattServices == null) {
//            return;
//        }
//        String uuid = null;
//
//        String unknownServiceString = getResources().getString(R.string.unknown_service);
//        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
//        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
//        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
//        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
//
//        for (BluetoothGattService gattService : gattServices) {
//            HashMap<String, String> currentServiceData = new HashMap<String, String>();
//            uuid = gattService.getUuid().toString();
//            currentServiceData.put(LIST_NAME, Constants.lookup(uuid, unknownServiceString));
//            currentServiceData.put(LIST_UUID, uuid);
//            gattServiceData.add(currentServiceData);
//
//            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
//            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
//            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();
//
//            // Loops through available Characteristics.
//            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
//                charas.add(gattCharacteristic);
//                HashMap<String, String> currentCharaData = new HashMap<String, String>();
//                uuid = gattCharacteristic.getUuid().toString();
//                currentCharaData.put(LIST_NAME, Constants.lookup(uuid, unknownCharaString));
//                currentCharaData.put(LIST_UUID, uuid);
//                gattCharacteristicGroupData.add(currentCharaData);
//            }
//            mGattCharacteristics.add(charas);
//            gattCharacteristicData.add(gattCharacteristicGroupData);
//        }
//    }


}
