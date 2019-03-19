package com.wisethan.ble.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class BleModel implements Serializable {
    private String mDeviceId;
    private String mMAC;
    private String mName;
    private String mDescription;
    private String mScanRecord;

    public BleModel() {
        mDeviceId = "";
        mMAC = "";
        mName = "";
        mDescription = "";
        mScanRecord = "";
    }

    public BleModel(Map<String, Object> data) {
        if (data.containsKey("device_id")) {
            mDeviceId = (String) data.get("device_id");
        } else {
            mDeviceId = "";
        }

        if (data.containsKey("mac")) {
            mMAC = (String) data.get("mac");
        } else {
            mMAC = "";
        }

        if (data.containsKey("name")) {
            mName = (String) data.get("name");
        } else {
            mName = "";
        }

        if (data.containsKey("description")) {
            mDescription = (String) data.get("description");
        } else {
            mDescription = "";
        }

        if (data.containsKey("scan_record")) {
            mScanRecord = (String) data.get("scan_record");
        } else {
            mScanRecord = "";
        }
    }

    public void setDeviceId(String deviceid) {
        mDeviceId = deviceid;
    }

    public void setMAC(String mac) {
        mMAC = mac;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public void setScanRecord(String scanRecord) {
        mScanRecord = scanRecord;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public String getMAC() {
        return mMAC;
    }

    public String getName() {
        return mName;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getScanRecord() {
        return mScanRecord;
    }

    public Map<String, Object> getData() {
        Map<String, Object> data = new HashMap<>();

        data.put("device_id", mDeviceId);
        data.put("mac", mMAC);
        data.put("name", mName);
        data.put("description", mDescription);
        data.put("scan_record", mScanRecord);

        return data;
    }
}
