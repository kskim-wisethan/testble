package com.wisethan.ble;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
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

    String uuid ="";
    String co2 ="";
    String temp ="";
    String humidity ="";

    private  String PENDING_ACTION = "com.wisethan.ble.Pending_Action";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();

        if(action.equals(PENDING_ACTION)){
            Toast.makeText(context, "refresh_iv Click", Toast.LENGTH_SHORT).show();

            ((MainActivity)MainActivity.mcontext).DataSet();


        }else {
            SharedPreferences sharedPreferences = context.getSharedPreferences("SHARE_PREF",Context.MODE_PRIVATE);
            uuid = sharedPreferences.getString("uuid","!");
            co2 = sharedPreferences.getString("co2","!");
            temp = sharedPreferences.getString("temp","!");
            humidity = sharedPreferences.getString("humidity","!");

            AppWidgetManager app = AppWidgetManager.getInstance(context);
            int ids[] = app.getAppWidgetIds(new ComponentName(context, this.getClass()));
            for (int i = 0; i < ids.length; i++) {
                updateAppWidget(context, app, ids[i]);
            }
        }

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


    private PendingIntent getPendingIntent(Context context, int id) {
        Intent intent = new Intent(context, WidgetProvider.class);
        intent.setAction(PENDING_ACTION);
        intent.putExtra("viewId", id);
        return PendingIntent.getBroadcast(context, id, intent, 0);
    }

    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager,int appWidgetId) {


        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setOnClickPendingIntent(R.id.refresh_iv, getPendingIntent(context, R.id.refresh_iv));
        views.setTextViewText(R.id.temp_widget_tv, temp+"˚");
        views.setTextViewText(R.id.humidity_widget_tv, humidity+"˚");
        views.setTextViewText(R.id.co2_widget_tv, co2+" ppm");

        appWidgetManager.updateAppWidget(appWidgetId,views);

    }



}
