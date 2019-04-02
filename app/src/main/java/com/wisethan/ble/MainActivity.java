package com.wisethan.ble;

import android.Manifest;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.wisethan.ble.model.BleModel;
import com.wisethan.ble.service.BluetoothLeService;
import com.wisethan.ble.util.AdapterSpinner1;
import com.wisethan.ble.util.BleManager;
import com.wisethan.ble.util.Constants;
import com.wisethan.ble.util.PermissionManager;
import com.wisethan.ble.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    Button scan_btn;
    Spinner ble_spinner;
    TextView mTempTv;
    TextView mCO2Tv;
    TextView mHumidityTv;
    Button write_bt;
    Button read_bt;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_FINE_LOCATION = 2;
    private boolean mBound = false;
    public int num =0;


    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private boolean mConnected = false;
    private BleModel mModel;
    private long lastTimeBackPressed;


    private int mServiceIndex = 0;
    private int mCharacteristicIndex = 0;

    public ArrayList<String> uuid = new ArrayList<String>();
    ArrayList<String> mItems = new ArrayList<String>();
    String pairingDevice = "";
    ArrayList<BleModel> mDevices = new ArrayList<BleModel>();

    String mDeviceUUID = "";
    BleManager mBleManager;
    AdapterSpinner1 adapterSpinner1;
    LocationManager locationManager;

    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    public static Context mcontext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE); //GPS 켜짐 상태 유무 확인 후 꺼져있으면 GPS창 이동
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            startActivity(intent);
        }
        mcontext = this;
        PermissionManager permissionManager = new PermissionManager(this);
        permissionManager.permissionCheck();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ble_spinner = findViewById(R.id.ble_spinner);
        scan_btn = findViewById(R.id.ble_scan_btn);
        mTempTv = findViewById(R.id.temp_tv);
        mCO2Tv = findViewById(R.id.co2_tv);
        mHumidityTv = findViewById(R.id.humidity_tv);
        write_bt = findViewById(R.id.write_bt);
        read_bt = findViewById(R.id.read_bt);

        SharedPreferences sharedPreferences = getSharedPreferences("UUID", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("uuid", "");
        editor.commit();
        Intent intent = new Intent(MainActivity.this, WidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        MainActivity.this.sendBroadcast(intent);

        mItems.add("선택");
        mDevices.add(new BleModel());

        mBleManager = BleManager.getInstance(this);
        BLEscan();

        adapterSpinner1 = new AdapterSpinner1(this, mDevices, pairingDevice);
        ble_spinner.setAdapter(adapterSpinner1);
        adapterSpinner1.notifyDataSetChanged();

        ble_spinner.setOnItemSelectedListener(this);
        read_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataSet();
                Toast.makeText(getApplicationContext(), "Read", Toast.LENGTH_SHORT).show();
            }
        });
        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "refresh", Toast.LENGTH_SHORT).show();
                Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_anim);
                scan_btn.startAnimation(anim);

                mBound = false;
                try{
                    unbindService(mServiceConnection);
                }catch (Exception e){
                    scan();
                }
                scan();
            }
        });
    }

    public void scan(){
        uuid.clear();
        mItems.clear();
        mDevices.clear();
        mItems.add("선택");
        mDevices.add(new BleModel());
        ble_spinner.setSelection(0);
        mHumidityTv.setText("--˚");
        mTempTv.setText("--˚");
        mCO2Tv.setText("-- ppm");
        SharedPreferences sharedPreferences = getSharedPreferences("UUID", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("uuid", "");
        editor.commit();
        BLEscan();
        Intent intent = new Intent(MainActivity.this, WidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        MainActivity.this.sendBroadcast(intent);
    }

    public void DataSet() { //연결된 블루투스의 데이터 가져오기
        mServiceIndex = 0;
        mCharacteristicIndex = 0;
        requestCharacteristicValue();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mModel.getUuid());
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mGattUpdateReceiver);
        if(mBound){
            unbindService(mServiceConnection);
            mBound = false;
        }
        //unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    public void BLEscan() {
        mBleManager.scanBleDevice(new BleManager.BleDeviceCallback() {   //블루투스 기기 검색할때마다 콜백함

            @Override
            public void onResponse(BleModel model) {
                if (Constants.SCAN_FILTER.isEmpty()) {
                    addDevice(model);
                } else {
                    String id = model.getUuid();
                    if (id.compareTo(Constants.SCAN_FILTER) == 0) {
                        mBleManager.stopScan();
                        addDevice(model);
                    }
                }
            }
        });
    }

    private void addDevice(BleModel model) { //검색된 기기 ArrayList에 넣고 스피너에 뿌려주기
        String id = model.getUuid();
        int index = findDeviceId(id);

        if (index >= 0) {
            mDevices.set(index, model);
            mItems.set(index, (model.getName() == null) ? getString(R.string.unknown_device) : model.getName());
        } else {
            mDevices.add(model);
            mItems.add((model.getName() == null) ? getString(R.string.unknown_device) : model.getName());
        }
        adapterSpinner1.notifyDataSetChanged();
    }

    private int findDeviceId(String id) {
        int ret = -1;
        for (int i = 0; i < mDevices.size(); i++) {
            if (mDevices.get(i).getUuid().compareTo(id) == 0) {
                ret = i;
            }
        }
        return ret;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (!mDevices.get(position).getUuid().equals("")) {
            mDeviceUUID = mDevices.get(position).getUuid();
        }
        if(mBound){
            unbindService(mServiceConnection);
            Intent intent = new Intent(MainActivity.this, WidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            MainActivity.this.sendBroadcast(intent);
            mHumidityTv.setText("--˚");
            mTempTv.setText("--˚");
            mCO2Tv.setText("-- ppm");
        }
        if (position > 0) {
            SharedPreferences sharedPreferences = getSharedPreferences("UUID", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("uuid", mDeviceUUID);
            editor.commit();
            mModel = mDevices.get(position);
            Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
            DataSet();
            mBound = true;
            Toast.makeText(getApplicationContext(), "Read", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:  //블루투스 on/off 버튼 클릭시 받아오는 코드
                if (resultCode == Activity.RESULT_OK) {
                    System.out.println("bluetooth ok");
                    BLEscan();
                } else {
                    // 취소 눌렀을 때
                }
                break;
            case REQUEST_FINE_LOCATION: //GPS 권한 허용시 받아오는 코드
                if (resultCode == Activity.RESULT_OK) {
                    System.out.println("GPS ok");
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivity(intent);
                } else {
                    // 취소 눌렀을 때
                }
                break;
        }
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {  //블루투스 연결하는 부분
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {

            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            mBluetoothLeService.connect(mModel.getUuid());
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            mBound = false;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.gatt_connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.gatt_disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                displayData(intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA_BYTES));
            }
        }
    };

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            //mDataField.setText(data);
        }
    }

    private void displayData(final byte[] data) {  //연결된 블루투스 데이터를 받아서 저장후 위젯에 브로드캐스트날려줌
        if (data != null) {
            String serviceName = Constants.lookup(mGattCharacteristics.get(mServiceIndex).get(mCharacteristicIndex).getService().getUuid().toString(), getString(R.string.unknown_service));
            if (mCharacteristicIndex == 0) {
                String serviceTitle = "[[ " + serviceName + " ]]";
                //mFragment.addList(serviceTitle);
            }

            String uuidString = mGattCharacteristics.get(mServiceIndex).get(mCharacteristicIndex).getUuid().toString();
            String characteristicValue = "> " + Constants.lookup(uuidString, getString(R.string.unknown_characteristic)) + " : ";

            if (serviceName.compareTo("Custom Service") == 0) {
                String characteristicValueHex = StringUtils.byteArrayInIntegerFormat(data);
                characteristicValue += characteristicValueHex;

                SharedPreferences sharedPreferences = getSharedPreferences("SHARE_PREF", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("uuid", mDeviceUUID);

                if (uuidString.compareTo(Constants.CUSTOM_CHARACTERISTIC1) == 0) {
                    // CO2
                    mCO2Tv.setText(characteristicValueHex + " ppm");
                    editor.putString("co2", characteristicValueHex);

                } else if (uuidString.compareTo(Constants.CUSTOM_CHARACTERISTIC2) == 0) {
                    // Temperature
                    mTempTv.setText(characteristicValueHex + "˚");
                    editor.putString("temp", characteristicValueHex);

                } else if (uuidString.compareTo(Constants.CUSTOM_CHARACTERISTIC3) == 0) {
                    // Humidity
                    mHumidityTv.setText(characteristicValueHex + "˚");
                    editor.putString("humidity", characteristicValueHex);

                } else if (uuidString.compareTo(Constants.CUSTOM_CHARACTERISTIC4) == 0) {
                    // Update Period

                }
                num++;
                if(num==4){
                    editor.commit();
                    Intent intent = new Intent(MainActivity.this, WidgetProvider.class);
                    intent.setAction(AppWidgetManager.ACTION_APPWIDGET_BIND);
                    MainActivity.this.sendBroadcast(intent);
                    num=0;
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

    private void displayGattServices(List<BluetoothGattService> gattServices) { //BluetoothLeService에서 처리된 후 보내준 상태에 따라 UI를 고쳐줌
        if (gattServices == null) {
            return;
        }
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
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

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void clearUI() {
        //mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        //mDataField.setText(R.string.no_data);
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

    @Override
    public void onBackPressed()
    {
        //2초 이내에 뒤로가기 버튼을 재 클릭 시 앱 종료
        if (System.currentTimeMillis() - lastTimeBackPressed < 2000)
        {
            finishAffinity();
            System.runFinalization();
            System.exit(0);
            return;
        }
        Toast.makeText(this, "'뒤로' 버튼을 한번 더 누르시면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
        //lastTimeBackPressed에 '뒤로'버튼이 눌린 시간을 기록
        lastTimeBackPressed = System.currentTimeMillis();
    }
}
