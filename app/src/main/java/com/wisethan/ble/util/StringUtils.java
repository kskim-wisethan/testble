package com.wisethan.ble.util;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class StringUtils {
    private static final String TAG = "StringUtils";

    private static String byteToHex(byte b) {
        char char1 = Character.forDigit((b & 0xF0) >> 4, 16);
        char char2 = Character.forDigit((b & 0x0F), 16);

        return String.format("0x%1$s%2$s", char1, char2);
    }

    public static String byteArrayInHexFormat(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{ ");
        for (int i = 0; i < byteArray.length; i++) {
            if (i > 0) {
                stringBuilder.append(", ");
            }
            String hexString = byteToHex(byteArray[i]);
            stringBuilder.append(hexString);
        }
        stringBuilder.append(" }");

        return stringBuilder.toString();
    }

    public static String byteArrayInIntegerFormat(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }

        ByteBuffer wrapped = ByteBuffer.wrap(byteArray);
        int value = 0;
        if (byteArray.length == Byte.BYTES) {
            value = byteArray[0];
        } else if (byteArray.length == Short.BYTES) {
            value = wrapped.getShort();
        } else if (byteArray.length == Integer.BYTES) {
            value = wrapped.getInt();
        }
        String valueString = String.valueOf(value);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(valueString);

        return stringBuilder.toString();
    }

    public static byte[] bytesFromString(String string) {
        byte[] stringBytes = new byte[0];
        try {
            stringBytes = string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Failed to convert message string to byte array");
        }

        return stringBytes;
    }

    public static String stringFromBytes(byte[] bytes) {
        String byteString = null;
        try {
            byteString = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Unable to convert message bytes to string");
        }
        return byteString;
    }

    public static boolean checkString(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == 0x00) {
                return false;
            }
            if (bytes[i] > 0x7E) {
                return false;
            }
            if (bytes[i] < 0x20) {
                if (bytes[i] != 0x09 && bytes[i] != 0x0A && bytes[i] != 0x0D && bytes[i] != 0x1B) {
                    return false;
                }
                if (bytes[i] == 0x1B || i == bytes.length - 1) {
                    return false;
                }
            }
        }
        return true;
    }
}
