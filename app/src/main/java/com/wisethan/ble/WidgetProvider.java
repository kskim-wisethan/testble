package com.wisethan.ble;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.wisethan.ble.service.BluetoothLeService;
import com.wisethan.ble.util.Constants;
import com.wisethan.ble.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class WidgetProvider extends AppWidgetProvider {

    private int mServiceIndex = 0;
    private int mCharacteristicIndex = 0;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    public Context con;
    public int num = 0;

    String uuid = null;
    String co2 = "--";
    String temp = "--";
    String humidity = "--";

    private static PendingIntent mSender;
    private static AlarmManager mManager;
    private String PENDING_ACTION = "com.wisethan.ble.Pending_Action";

    private static final int WIDGET_UPDATE_INTERVAL = 1800000;   //30분마다 위젯 update해줌
//    private static final int WIDGET_UPDATE_INTERVAL = 15000;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        con=context;
        if (action.equals(PENDING_ACTION) || action.equals("android.appwidget.action.APPWIDGET_UPDATE")) {  //30분마다 실행 or refresh 버튼 누를 때 실행
            long firstTime = System.currentTimeMillis() + WIDGET_UPDATE_INTERVAL;
            mSender = PendingIntent.getBroadcast(context, 0, intent, 0);
            mManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            mManager.set(AlarmManager.RTC, firstTime, mSender);

            co2 = "--";
            temp = "--";
            humidity = "--";


            SharedPreferences sharedPreferences = context.getSharedPreferences("Broadcast", Context.MODE_PRIVATE);  // 메인의 브로드캐스트와 구별을 위해 key"broad" value"widget"을 저장함
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("broad", "widget");
            editor.commit();
            Toast.makeText(context, "Read", Toast.LENGTH_SHORT).show();
            Intent gattServiceIntent = new Intent(context.getApplicationContext(), BluetoothLeService.class);   //ble 연결 후 데이터 받아오기 시작
            context.getApplicationContext().bindService(gattServiceIntent, mServiceConnection, context.getApplicationContext().BIND_AUTO_CREATE);
            context.getApplicationContext().registerReceiver(mGattUpdateReceiver1, makeGattUpdateIntentFilter());

            refresh(con);
        } else if (action.equals("android.appwidget.action.APPWIDGET_BIND")) { // MainActivity에서 ble 값을 받아 오고나서 실행
            SharedPreferences sharedPreferences = context.getSharedPreferences("SHARE_PREF1", Context.MODE_PRIVATE);
            uuid = sharedPreferences.getString("uuid", null);
            co2 = sharedPreferences.getString("co2", "0");
            temp = sharedPreferences.getString("temp", "0");
            humidity = sharedPreferences.getString("humidity", "0");
            Toast.makeText(context, "Data Set", Toast.LENGTH_SHORT).show();
            refresh(con);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));

//        ((MainActivity) MainActivity.mcontext).DataSet();

        System.out.println("Widget Update ok");
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

    public void refresh(Context context){
        AppWidgetManager app = AppWidgetManager.getInstance(context);
        int ids[] = app.getAppWidgetIds(new ComponentName(context, this.getClass()));
        for (int i = 0; i < ids.length; i++) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            views.setOnClickPendingIntent(R.id.refresh_iv, getPendingIntent(context.getApplicationContext(), R.id.refresh_iv));
            views.setTextViewText(R.id.temp_widget_tv, temp + "˚");
            views.setTextViewText(R.id.humidity_widget_tv, humidity + "%");
            views.setTextViewText(R.id.co2_widget_tv, co2 + " ppm");
            app.updateAppWidget(ids[i], views);
        }
    }

    private PendingIntent getPendingIntent(Context context, int id) {
        Intent intent = new Intent(context, WidgetProvider.class);
        intent.setAction(PENDING_ACTION);
        intent.putExtra("viewId", id);
        return PendingIntent.getBroadcast(context, id, intent, 0);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED_WIDGET);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED_WIDGET);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED_WIDGET);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE_WIDGET);
        return intentFilter;
    }

    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {  //위젯 내용을 update해주는 부분
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setOnClickPendingIntent(R.id.refresh_iv, getPendingIntent(context.getApplicationContext(), R.id.refresh_iv));
        views.setTextViewText(R.id.temp_widget_tv, temp + "˚");
        views.setTextViewText(R.id.humidity_widget_tv, humidity + "%");
        views.setTextViewText(R.id.co2_widget_tv, co2 + " ppm");
        con = context;
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public void DataSet() { //연결된 블루투스의 데이터 가져오기
        mServiceIndex = 0;
        mCharacteristicIndex = 0;
        requestCharacteristicValue();
    }

    void requestCharacteristicValue() {  //ble 특성이 변화 될 때 처리하기위함
        if (mGattCharacteristics != null) {
            final BluetoothGattCharacteristic characteristic = mGattCharacteristics.get(mServiceIndex).get(mCharacteristicIndex);
            final int charaProp = characteristic.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                if (mNotifyCharacteristic != null) {
                    mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
                    mNotifyCharacteristic = null;
                }
                mBluetoothLeService.readCharacteristic(characteristic);
            }
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                mNotifyCharacteristic = characteristic;
                mBluetoothLeService.setCharacteristicNotification(characteristic, true);
            }
        }
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {  //블루투스 연결하는 부분
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {

            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            mBluetoothLeService.disconnect();
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();

            if (!mBluetoothLeService.initialize()) {
                //finish();
            }

            SharedPreferences sharedPreferences = con.getSharedPreferences("SHARE_PREF1", Context.MODE_PRIVATE); //마지막에 연결된 uuid값을 가져와서 ble 연결시킴
            uuid = sharedPreferences.getString("uuid", null);

            if(uuid!=null){
                mBluetoothLeService.connect(uuid);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };


    public final BroadcastReceiver mGattUpdateReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try{
                final String action = intent.getAction();
                if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED_WIDGET.equals(action)) {
                    displayGattServices(mBluetoothLeService.getSupportedGattServices());
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE_WIDGET.equals(action)) {
                    displayData(intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA_BYTES_WIDGET));
                }
            }catch (Exception e){

            }

        }
    };

    private void displayGattServices(List<BluetoothGattService> gattServices) { //BluetoothLeService에서 처리된 후 보내준 상태에 따라 UI를 고쳐줌
        if (gattServices == null) {
            return;
        }
        String uuid = null;
        String unknownServiceString = con.getResources().getString(R.string.unknown_service);
        String unknownCharaString = con.getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_NAME, Constants.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(LIST_NAME, Constants.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
        DataSet();
    }

    private void displayData(final byte[] data) {  //연결된 블루투스 데이터를 받아서 저장후 위젯에 브로드캐스트날려줌
        if (data != null) {
            String serviceName = Constants.lookup(mGattCharacteristics.get(mServiceIndex).get(mCharacteristicIndex).getService().getUuid().toString(), con.getString(R.string.unknown_service));
            if (mCharacteristicIndex == 0) {
                String serviceTitle = "[[ " + serviceName + " ]]";
                //mFragment.addList(serviceTitle);
            }

            String uuidString = mGattCharacteristics.get(mServiceIndex).get(mCharacteristicIndex).getUuid().toString();
            String characteristicValue = "> " + Constants.lookup(uuidString, con.getString(R.string.unknown_characteristic)) + " : ";

            if (serviceName.compareTo("Custom Service") == 0) {
                String characteristicValueHex = StringUtils.byteArrayInIntegerFormat(data);
                characteristicValue += characteristicValueHex;


                if (uuidString.compareTo(Constants.CUSTOM_CHARACTERISTIC1) == 0) {
                    co2 = characteristicValueHex;
                } else if (uuidString.compareTo(Constants.CUSTOM_CHARACTERISTIC2) == 0) {
                    temp = characteristicValueHex;
                } else if (uuidString.compareTo(Constants.CUSTOM_CHARACTERISTIC3) == 0) {
                    humidity = characteristicValueHex;
                } else if (uuidString.compareTo(Constants.CUSTOM_CHARACTERISTIC4) == 0) {
                    // Update Period
                }

                num++;
                if (num == 4) {
                    AppWidgetManager app = AppWidgetManager.getInstance(con);
                    int ids[] = app.getAppWidgetIds(new ComponentName(con, this.getClass()));
                    for (int i = 0; i < ids.length; i++) {
                        RemoteViews views = new RemoteViews(con.getPackageName(), R.layout.widget_layout);
                        views.setTextViewText(R.id.temp_widget_tv, temp + "˚");
                        views.setTextViewText(R.id.humidity_widget_tv, humidity + "%");
                        views.setTextViewText(R.id.co2_widget_tv, co2 + " ppm");
                        app.updateAppWidget(ids[i], views);
                    }
                }


            } else if (StringUtils.checkString(data)) {
                String characteristicValueString = StringUtils.stringFromBytes(data);
                characteristicValue += characteristicValueString;
            } else {
                String characteristicValueHex = StringUtils.byteArrayInHexFormat(data);
                characteristicValue += characteristicValueHex;
            }
            //mFragment.addList(characteristicValue);

            ++mCharacteristicIndex;
            int characteristicsLength = mGattCharacteristics.get(mServiceIndex).size();
            if (mCharacteristicIndex >= characteristicsLength) {
                ++mServiceIndex;
                mCharacteristicIndex = 0;
                int servicesLength = mGattCharacteristics.size();
                if (mServiceIndex < servicesLength && mGattCharacteristics.get(mServiceIndex).size() == 0) {
                    ++mServiceIndex;
                }
                if (mServiceIndex >= servicesLength) {
                    mServiceIndex = -1;
                    mCharacteristicIndex = -1;
                }
            }
        }
        if (mServiceIndex >= 0 && mCharacteristicIndex >= 0) {
            requestCharacteristicValue();
        }
    }
}
