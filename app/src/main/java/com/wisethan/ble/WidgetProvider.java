package com.wisethan.ble;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.wisethan.ble.service.BluetoothLeService;


public class WidgetProvider extends AppWidgetProvider {

    String uuid = "";
    String co2 = "";
    String temp = "";
    String humidity = "";

    BluetoothLeService ble ;
    private static PendingIntent mSender;
    private static AlarmManager mManager;
    private String PENDING_ACTION = "com.wisethan.ble.Pending_Action";

    private static final int WIDGET_UPDATE_INTERVAL = 1800000;   //30분마다 위젯 update해줌
//    private static final int WIDGET_UPDATE_INTERVAL = 15000;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();

        if (action.equals(PENDING_ACTION)||action.equals("android.appwidget.action.APPWIDGET_UPDATE")) {  //30분마다 실행 or refresh 버튼 누를 때 실행
            long firstTime = System.currentTimeMillis() + WIDGET_UPDATE_INTERVAL;
            mSender = PendingIntent.getBroadcast(context, 0, intent, 0);
            mManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            mManager.set(AlarmManager.RTC, firstTime, mSender);

            uuid = "--";
            co2 = "--";
            temp = "--";
            humidity = "--";

            ((MainActivity) MainActivity.mcontext).DataSet();  // MainActivity의 DataSet() 메서드를 실행시켜 ble값을 다시 받아옴
            if(action.equals(PENDING_ACTION)){
                Toast.makeText(context, "Read", Toast.LENGTH_SHORT).show();
            }
        } else if(action.equals("android.appwidget.action.APPWIDGET_BIND")){ // ble 값을 받아 오고나서 실행
            SharedPreferences sharedPreferences = context.getSharedPreferences("SHARE_PREF", Context.MODE_PRIVATE);
            uuid = sharedPreferences.getString("uuid", "0");
            co2 = sharedPreferences.getString("co2", "0");
            temp = sharedPreferences.getString("temp", "0");
            humidity = sharedPreferences.getString("humidity", "0");
            Toast.makeText(context, "Data Set", Toast.LENGTH_SHORT).show();
        }
        AppWidgetManager app = AppWidgetManager.getInstance(context);
        int ids[] = app.getAppWidgetIds(new ComponentName(context, this.getClass()));
        for (int i = 0; i < ids.length; i++) {
            updateAppWidget(context, app, ids[i]);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));

        ((MainActivity) MainActivity.mcontext).DataSet();

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

    private PendingIntent getPendingIntent(Context context, int id) {
        Intent intent = new Intent(context, WidgetProvider.class);
        intent.setAction(PENDING_ACTION);
        intent.putExtra("viewId", id);
        return PendingIntent.getBroadcast(context, id, intent, 0);
    }

    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {  //위젯 내용을 update해주는 부분
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setOnClickPendingIntent(R.id.refresh_iv, getPendingIntent(context, R.id.refresh_iv));
        views.setTextViewText(R.id.temp_widget_tv, temp + "˚");
        views.setTextViewText(R.id.humidity_widget_tv, humidity + "˚");
        views.setTextViewText(R.id.co2_widget_tv, co2 + " ppm");

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
