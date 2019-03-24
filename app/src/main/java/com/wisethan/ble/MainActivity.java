package com.wisethan.ble;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
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

    ArrayList<String> mItems = new ArrayList<String>();
    ArrayList<BleModel> mDevices = new ArrayList<BleModel>();

    int mScanCount = 0;
    String mOutput = "";
    String mDeviceName ="";
    ArrayAdapter<String> mAdapter;
    BleManager mBleManager;
    int i=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        mItems.add("선택");
        mDevices.add(new BleModel());
        mAdapter = new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_spinner_item, mItems);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ble_spinner.setAdapter(mAdapter);
        ble_spinner.setOnItemSelectedListener(this);


        mBleManager = BleManager.getInstance(this);
        BLEscan();

        //ble_spinner의 리스트 중복체크 필요


        write_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.WriteToProperty();
            }
        });
        read_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("BLE save", Utils.ReadToProperty());
                Toast.makeText(getApplicationContext(),Utils.ReadToProperty(),Toast.LENGTH_SHORT).show();
            }
        });
        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BLEscan();
            }
        });

    }

    public void BLEscan(){
        mBleManager.scanBleDevice(new BleManager.BleDeviceCallback() {
            @Override
            public void onResponse(BleModel model) {

                String id = model.getDeviceId();
                i++;

                System.out.println("i: "+i+ " bleModel: "+id );

                if (id.compareTo("C4:64:E3:F0:2E:65") == 0) {
                    ++mScanCount;
                    if (mScanCount > 1) {
                        mBleManager.stopScan();
                        return;
                    }
                    String name = (model.getName() == null) ? getString(R.string.unknown_device) : model.getName();
                    String record = model.getScanRecord();
                    mItems.add(name);
                } else {
                    mItems.add(id);
                }

                String name = (model.getName() == null) ? getString(R.string.unknown_device) : model.getName();
                String record = model.getScanRecord();
                mItems.add(name);

                mDevices.add(model);
                mAdapter.notifyDataSetChanged();
            }
        });


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
        Toast.makeText(this.getApplicationContext(), mDevices.get(position).getName(), Toast.LENGTH_SHORT).show();

        Log.v("aaaaa","position"+position);
        mDeviceName=parent.getSelectedItem().toString();
        System.out.println(parent.getSelectedItem().toString()+"!!!!!!!!!!!!!!");
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
