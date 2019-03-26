package com.wisethan.ble.util;

import java.util.HashMap;
import java.util.UUID;

public class Constants {
    public static String SERVICE_STRING = "7D2EA28A-F7BD-485A-BD9D-92AD6ECFE93E";
    public static UUID SERVICE_UUID = UUID.fromString(SERVICE_STRING);

    public static String CHARACTERISTIC_ECHO_STRING = "7D2EBAAD-F7BD-485A-BD9D-92AD6ECFE93E";
    public static UUID CHARACTERISTIC_ECHO_UUID = UUID.fromString(CHARACTERISTIC_ECHO_STRING);

    public static String CHARACTERISTIC_TIME_STRING = "7D2EDEAD-F7BD-485A-BD9D-92AD6ECFE93E";
    public static UUID CHARACTERISTIC_TIME_UUID = UUID.fromString(CHARACTERISTIC_TIME_STRING);
    public static String CLIENT_CONFIGURATION_DESCRIPTOR_STRING = "00002902-0000-1000-8000-00805f9b34fb";
    public static UUID CLIENT_CONFIGURATION_DESCRIPTOR_UUID = UUID.fromString(CLIENT_CONFIGURATION_DESCRIPTOR_STRING);

    public static final String CLIENT_CONFIGURATION_DESCRIPTOR_SHORT_ID = "2902";

    public static final long SCAN_PERIOD = 5000;
    public static String SCAN_FILTER = "";      // "C4:64:E3:F0:2E:65";

    // sample gatt attributes
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String TEMPERATURE = "00002a6e-0000-1000-8000-00805f9b34fb";
    public static String HUMIDITY = "00002a6f-0000-1000-8000-00805f9b34fb";

    public static String CUSTOM_CHARACTERISTIC1 = "f000ee01-0451-4000-b000-000000000000";
    public static String CUSTOM_CHARACTERISTIC2 = "f000ee02-0451-4000-b000-000000000000";
    public static String CUSTOM_CHARACTERISTIC3 = "f000ee03-0451-4000-b000-000000000000";
    public static String CUSTOM_CHARACTERISTIC4 = "f000ee04-0451-4000-b000-000000000000";

    public static String CHARACTERISTIC_USER_DESCRIPTION = "00002901-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    static {
        // Sample Services.
        attributes.put("00001800-0000-1000-8000-00805f9b34fb", "Generic Access");
        attributes.put("00001801-0000-1000-8000-00805f9b34fb", "Generic Attribute");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information");
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000181a-0000-1000-8000-00805f9b34fb", "Environmental Sensing");
        attributes.put("f000ee00-0451-4000-b000-000000000000", "Custom Service");

        // Sample Characteristics.
        attributes.put("00002a00-0000-1000-8000-00805f9b34fb", "Device Name");
        attributes.put("00002a01-0000-1000-8000-00805f9b34fb", "Appearance");
        attributes.put("00002a04-0000-1000-8000-00805f9b34fb", "Peripheral Preferred Connection Parameters");

        attributes.put("00002a23-0000-1000-8000-00805f9b34fb", "System ID");
        attributes.put("00002a24-0000-1000-8000-00805f9b34fb", "Model Number String");
        attributes.put("00002a25-0000-1000-8000-00805f9b34fb", "Serial Number String");
        attributes.put("00002a26-0000-1000-8000-00805f9b34fb", "Firmware Revision String");
        attributes.put("00002a27-0000-1000-8000-00805f9b34fb", "Hardware Revision String");
        attributes.put("00002a28-0000-1000-8000-00805f9b34fb", "Software Revision String");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put("00002a2a-0000-1000-8000-00805f9b34fb", "IEEE 11073-20601 Regulatory Certification Data List");
        attributes.put("00002a50-0000-1000-8000-00805f9b34fb", "PnP ID");

        attributes.put("f000ee01-0451-4000-b000-000000000000", "CO2 Value");
        attributes.put("f000ee02-0451-4000-b000-000000000000", "Temperature Value");
        attributes.put("f000ee03-0451-4000-b000-000000000000", "Humidity Value");
        attributes.put("f000ee04-0451-4000-b000-000000000000", "Update Period");

        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put(TEMPERATURE, "Temperature");
        attributes.put(HUMIDITY, "Humidity");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
