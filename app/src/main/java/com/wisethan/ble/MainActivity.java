package com.wisethan.ble;

import android.os.Bundle;
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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    Button scan_btn;

    Spinner ble_spinner;
    TextView temp_tv;
    TextView co2_tv;
    TextView humidity_tv_tv;
    ArrayList<String> mItems = new ArrayList<String>();
    ArrayList<BleModel> mDevices = new ArrayList<BleModel>();

    int mScanCount = 0;
    String mOutput = "";
    ArrayAdapter<String> mAdapter;
    BleManager mBleManager;

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

        mItems.add("선택");
        mDevices.add(new BleModel());
        mAdapter = new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_spinner_item, mItems);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ble_spinner.setAdapter(mAdapter);
        ble_spinner.setOnItemSelectedListener(this);

        mBleManager = BleManager.getInstance(this);
        mBleManager.scanBleDevice(new BleManager.BleDeviceCallback() {
            @Override
            public void onResponse(BleModel model) {
                String id = model.getDeviceId();
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
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
