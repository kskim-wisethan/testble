package com.wisethan.ble.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import java.util.ArrayList;

import com.wisethan.ble.MainActivity;


public class PermissionManager {
    private Context context;
    private ArrayList<String> permission_check_list = new ArrayList<>();

    public PermissionManager(Context context) {
        this.context = context;

        this.permission_check_list.add(Manifest.permission.INTERNET);
        this.permission_check_list.add(Manifest.permission.BLUETOOTH);
        this.permission_check_list.add(Manifest.permission.BLUETOOTH_ADMIN);
        this.permission_check_list.add(Manifest.permission.ACCESS_FINE_LOCATION);
        this.permission_check_list.add(Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    public void permissionCheck() {
        ArrayList<String> deniedPermission = new ArrayList<>();

        for(int i = 0; i < permission_check_list.size(); i++) {
            if (ContextCompat.checkSelfPermission(context, permission_check_list.get(i)) == PackageManager.PERMISSION_DENIED){
                Log.d("debug",permission_check_list.get(i));
                deniedPermission.add(permission_check_list.get(i));
            }
        }

        if (!deniedPermission.isEmpty()) {
            String[] permissions = new String [deniedPermission.size()];
            deniedPermission.toArray(permissions);
            ActivityCompat.requestPermissions((MainActivity)context, permissions, 0);
        }
    }
}