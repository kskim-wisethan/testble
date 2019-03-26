package com.wisethan.ble;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wisethan.ble.R;
import com.wisethan.ble.model.BleModel;

import java.util.ArrayList;
import java.util.List;

public class AdapterSpinner1 extends BaseAdapter {


    Context context;
    ArrayList<BleModel> data;
    ArrayList<String> pairingDevice = new ArrayList<String>();
    LayoutInflater inflater;


    public AdapterSpinner1(Context context, ArrayList<BleModel> data, ArrayList<String> pairingDevice){
        this.context = context;
        this.data = data;
        this.pairingDevice = pairingDevice;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        if(data!=null) return data.size();
        else return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null) {
            convertView = inflater.inflate(R.layout.spinner_spinner1_normal, parent, false);
        }

        if(data!=null){
            //데이터세팅
            String text = data.get(position).getName();
            ((TextView)convertView.findViewById(R.id.spinnerText)).setText(text);
        }

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            convertView = inflater.inflate(R.layout.spinner_spinner1_dropdown, parent, false);
        }

        //데이터세팅
        String text = data.get(position).getName();
        ((TextView)convertView.findViewById(R.id.spinnerText)).setText(text);

        for (int i = 0; i<pairingDevice.size();i++){
            if(pairingDevice.get(i).equals(data.get(position).getUuid())){
                convertView.setBackgroundColor(Color.parseColor("#C2C2C2"));
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

