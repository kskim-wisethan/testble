package com.wisethan.ble;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.wisethan.ble.model.BleModel;
import com.wisethan.ble.util.BleManager;
import com.wisethan.ble.util.Constants;
import com.wisethan.ble.util.PermissionManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    Button scan_btn;
    Spinner ble_spinner;
    TextView temp_tv;
    TextView co2_tv;
    TextView humidity_tv_tv;
    Button write_bt;
    Button read_bt;


    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_FINE_LOCATION = 2;

    ArrayList<String> mItems = new ArrayList<String>();
    ArrayList<String> pairingDevice = new ArrayList<String>();
    ArrayList<BleModel> mDevices = new ArrayList<BleModel>();

    int mScanCount = 0;
    String mOutput = "";
    String mDeviceName ="";
    ArrayAdapter<String> mAdapter;
    BleManager mBleManager;
    AdapterSpinner1 adapterSpinner1;

    LocationManager locationManager;



    int i=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            startActivity(intent);
        }


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PermissionManager permissionManager = new PermissionManager(this);
        permissionManager.permissionCheck();

        ble_spinner = findViewById(R.id.ble_spinner);

        scan_btn = findViewById(R.id.ble_scan_btn);
        temp_tv= findViewById(R.id.temp_tv);
        co2_tv = findViewById(R.id.co2_tv);
        humidity_tv_tv = findViewById(R.id.humidity_tv);
        write_bt = findViewById(R.id.write_bt);
        read_bt = findViewById(R.id.read_bt);

        mBleManager = BleManager.getInstance(this);
        BLEscan();

//        mItems.add("선택");
        mDevices.add(new BleModel());
//        mAdapter = new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_spinner_item, mItems);
//        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        ble_spinner.setAdapter(mAdapter);

        ble_spinner.setOnItemSelectedListener(this);

        adapterSpinner1 = new AdapterSpinner1(this, mDevices,pairingDevice);
        ble_spinner.setAdapter(adapterSpinner1);




        write_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WriteToProperty();
            }
        });
        read_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),ReadToProperty(),Toast.LENGTH_SHORT).show();
            }
        });
        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "refresh", Toast.LENGTH_SHORT).show();
                //adapterSpinner1.notifyDataSetChanged();
                BLEscan();
            }
        });
    }

    public void WriteToProperty(){
        File file = new File(Environment.getDataDirectory()+"/data/"+getPackageName(), mDeviceName);

        FileOutputStream fos = null;
        try{
            //property 파일이 없으면 생성
            if(!file.exists()){
                file.createNewFile();
            }

            fos = new FileOutputStream(file);

            //Property 데이터 저장
            Properties props = new Properties();
            props.setProperty("test" , "Property에서 데이터를 저장");   //(key , value) 로 저장
            props.store(fos, "Property Test");

            Log.d("prop", "write success");
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public String ReadToProperty(){
        //property 파일
        File file = new File(Environment.getDataDirectory()+"/data/"+getPackageName(), mDeviceName);

        if(!file.exists()){ return ""; }

        FileInputStream fis = null;
        String data = "";
        try{
            fis = new FileInputStream(file);

            //Property 데이터 읽기
            Properties props = new Properties();
            props.load(fis);
            data = props.getProperty("test1", "");  //(key , default value)

            Log.d("prop", "read success");
        }catch(IOException e){
            e.printStackTrace();
        }

        return data;
    }

    public void BLEscan(){

        mBleManager.scanBleDevice(new BleManager.BleDeviceCallback() {

            @Override
            public void onResponse(BleModel model) {

//                System.out.println("##############22");
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

        //System.out.println(mItems.size()+"@@@@@@"+model.getData());
        if (index >= 0) {
            mDevices.set(index, model);
//            mItems.set(index, (model.getName() == null) ? getString(R.string.unknown_device) : model.getName());
        } else {
            mDevices.add(model);
//            mItems.add((model.getName() == null) ? getString(R.string.unknown_device) : model.getName());
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

        Log.v("aaaaa","position"+position);
        mDeviceName=parent.getSelectedItem().toString();

        ble_spinner.setPrompt("선택");

//        System.out.println(parent.getSelectedItem().toString()+"!!!!!!!!!!!!!!");
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
}
