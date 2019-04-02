package com.wisethan.ble.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wisethan.ble.R;
import com.wisethan.ble.model.BleModel;

import java.util.ArrayList;

public class AdapterSpinner1 extends BaseAdapter {
    Context context;
    ArrayList<BleModel> data;
    String pairingDevice = "";
    LayoutInflater inflater;
    String uuid="";

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
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.spinner_spinner1_dropdown, parent, false);
        }

        if (data != null) {
            //데이터세팅
            String text = data.get(position).getName();
            TextView tv = (TextView) convertView.findViewById(R.id.spinnerText);
            tv.setText(text);

            SharedPreferences sharedPreferences = context.getSharedPreferences("UUID", Context.MODE_PRIVATE);
            uuid = sharedPreferences.getString("uuid", "");

            if (uuid != "" && uuid.equals(data.get(position).getUuid())) {
                tv.setBackgroundColor(Color.parseColor("#C2C2C2"));
            } else {
                tv.setBackgroundColor(Color.argb(0, 0, 0, 0));
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
