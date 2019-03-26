package com.wisethan.ble.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class BleModel implements Serializable {
    private String mUuid;
    private String mRssi;
    private String mName;
    private String mDescription;
    private String mScanRecord;

    public BleModel() {
        mUuid = "";
        mRssi = "";
        mName = "선택";
        mDescription = "";
        mScanRecord = "";
    }

    public BleModel(Map<String, Object> data) {
        if (data.containsKey("uuid")) {
            mUuid = (String) data.get("uuid");
        } else {
            mUuid = "";
        }

        if (data.containsKey("rssi")) {
            mRssi = (String) data.get("rssi");
        } else {
            mRssi = "";
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

    public void setUuid(String uuid) {
        mUuid = uuid;
    }

    public void setRssi(String rssi) {
        mRssi = rssi;
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

    public String getUuid() {
        return mUuid;
    }

    public String getRssi() {
        return mRssi;
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

        data.put("uuid", mUuid);
        data.put("rssi", mRssi);
        data.put("name", mName);
        data.put("description", mDescription);
        data.put("scan_record", mScanRecord);

        return data;
    }
}
