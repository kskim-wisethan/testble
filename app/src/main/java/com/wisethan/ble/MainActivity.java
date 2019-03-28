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
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.wisethan.ble.model.BleModel;
import com.wisethan.ble.service.BluetoothLeService;
import com.wisethan.ble.util.BleManager;
import com.wisethan.ble.util.Constants;
import com.wisethan.ble.util.PermissionManager;
import com.wisethan.ble.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener  {
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

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private boolean mConnected = false;
    private BleModel mModel;

    private int mServiceIndex = 0;
    private int mCharacteristicIndex = 0;

    ArrayList<String> mItems = new ArrayList<String>();
    String pairingDevice = "";
    ArrayList<BleModel> mDevices = new ArrayList<BleModel>();

    int mScanCount = 0;
    String mOutput = "";
    String mDeviceUUID = "";
    ArrayAdapter<String> mAdapter;
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

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
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

        mBleManager = BleManager.getInstance(this);
        BLEscan();

        mItems.add("선택");
        mDevices.add(new BleModel());
//        mAdapter = new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_spinner_item, mItems);
//        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        ble_spinner.setOnItemSelectedListener(this);

        adapterSpinner1 = new AdapterSpinner1(this, mDevices, pairingDevice);
        ble_spinner.setAdapter(adapterSpinner1);
        adapterSpinner1.notifyDataSetChanged();

        ble_spinner.setOnItemSelectedListener(this);
        write_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Write ok",Toast.LENGTH_SHORT).show();
                WriteToProperty();
            }
        });
        read_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataSet();
            }
        });
        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "refresh", Toast.LENGTH_SHORT).show();

                BLEscan();
            }
        });
    }

    public void  DataSet(){
        mServiceIndex = 0;
        mCharacteristicIndex = 0;
        requestCharacteristicValue();
        Toast.makeText(getApplicationContext(), "Data Read", Toast.LENGTH_SHORT).show();
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
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    public void WriteToProperty() {
        File file = new File(Environment.getDataDirectory() + "/data/" + getPackageName(), mDeviceUUID);

        FileOutputStream fos = null;
        try {
            //property 파일이 없으면 생성
            if (!file.exists()) {
                file.createNewFile();
            }

            fos = new FileOutputStream(file);

            //Property 데이터 저장
            Properties props = new Properties();
            props.setProperty("uuid", mDeviceUUID);   //(key , value) 로 저장
            props.store(fos, "Property Test");

            Log.d("prop", "write success");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String ReadToProperty() {
        //property 파일
        File file = new File(Environment.getDataDirectory() + "/data/" + getPackageName(), mDeviceUUID);

        if (!file.exists()) {
            return "";
        }

        FileInputStream fis = null;
        String data = "";
        try {
            fis = new FileInputStream(file);

            //Property 데이터 읽기
            Properties props = new Properties();
            props.load(fis);
            data = props.getProperty("uuid", "");  //(key , default value)

            Log.d("prop", "read success");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    public void BLEscan() {

        mBleManager.scanBleDevice(new BleManager.BleDeviceCallback() {

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

    private void addDevice(BleModel model) {
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
        //Toast.makeText(this.getApplicationContext(), mDevices.get(position).getName(), Toast.LENGTH_SHORT).show();


        Log.v("aaaaa", "position" + position);
        if (!mDevices.get(position).getUuid().equals("")) {
            mDeviceUUID = mDevices.get(position).getUuid();
        }

        if (position > 0) {

            mModel = mDevices.get(position);
            Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);


            SharedPreferences sharedPreferences = getSharedPreferences("SHARE_PREF",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("uuid", mDeviceUUID);
            editor.commit();

            Intent intent = new Intent(MainActivity.this, WidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            MainActivity.this.sendBroadcast(intent);

        }


    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    System.out.println("bluetooth ok");
                    BLEscan();
                } else {
                    // 취소 눌렀을 때
                }
                break;

            case REQUEST_FINE_LOCATION:
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

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            mBluetoothLeService.connect(mModel.getUuid());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Toast.makeText(getApplicationContext(), "브로드캐스트", Toast.LENGTH_SHORT).show();
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

    private void displayData(final byte[] data) {
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

                SharedPreferences sharedPreferences = getSharedPreferences("SHARE_PREF",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("uuid", mDeviceUUID);

                if (uuidString.compareTo(Constants.CUSTOM_CHARACTERISTIC1) == 0) {
                    // CO2
                    mCO2Tv.setText(characteristicValueHex+" ppm");
                    editor.putString("co2", characteristicValueHex);

                } else if (uuidString.compareTo(Constants.CUSTOM_CHARACTERISTIC2) == 0) {
                    // Temperature
                    mTempTv.setText(characteristicValueHex+"˚");
                    editor.putString("temp", characteristicValueHex);

                } else if (uuidString.compareTo(Constants.CUSTOM_CHARACTERISTIC3) == 0) {
                    // Humidity
                    mHumidityTv.setText(characteristicValueHex+"˚");
                    editor.putString("humidity", characteristicValueHex);

                } else if (uuidString.compareTo(Constants.CUSTOM_CHARACTERISTIC4) == 0) {
                    // Update Period

                }



                editor.commit();

                Intent intent = new Intent(MainActivity.this, WidgetProvider.class);
                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                MainActivity.this.sendBroadcast(intent);

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

    private void displayGattServices(List<BluetoothGattService> gattServices) {
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

    void requestCharacteristicValue() {
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@");
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


    public class AdapterSpinner1 extends BaseAdapter {


        Context context;
        ArrayList<BleModel> data;
        String pairingDevice = "";
        LayoutInflater inflater;


        public AdapterSpinner1(Context context, ArrayList<BleModel> data, String pairingDevice) {
            this.context = context;
            this.data = data;
            this.pairingDevice = pairingDevice;


            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }


        @Override
        public int getCount() {
            if (data != null) return data.size();
            else return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
//            if (convertView == null) {
//                convertView = inflater.inflate(R.layout.spinner_spinner1_normal, parent, false);
//            }

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.spinner_spinner1_normal, null);

            }
            if (data != null) {
                String text = data.get(position).getName();
                TextView tv = (TextView) convertView.findViewById(R.id.spinnerText);
                tv.setText(text);
            }



            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                convertView = inflater.inflate(R.layout.spinner_spinner1_dropdown, parent, false);
            }

            if (data != null) {
                //데이터세팅
                String text = data.get(position).getName();
                TextView tv = (TextView) convertView.findViewById(R.id.spinnerText);
                tv.setText(text);

                if(mDeviceUUID != "" && mDeviceUUID.equals(data.get(position).getUuid())){
                    tv.setBackgroundColor(Color.parseColor("#C2C2C2"));
                }else{
                    tv.setBackgroundColor(Color.argb(0,0,0,0));
                }

            }

            return convertView;
        }




        @Override
        public Object getItem(int position) {

            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }
}
