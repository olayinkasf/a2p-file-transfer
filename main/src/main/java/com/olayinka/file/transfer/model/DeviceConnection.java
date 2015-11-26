package com.olayinka.file.transfer.model;

import com.olayinka.file.transfer.AbstractAppSettings;
import ripped.android.json.JSONObject;

/**
 * Created by Olayinka on 11/26/2015.
 */
public class DeviceConnection {

    public static final String AUTH_RESULT = "auth.result";
    public static final String AUTH_CODE = "auth.code";


    private JSONObject mResult;
    private Device mDevice;

    public DeviceConnection(JSONObject result, Device device) {
        this.mResult = result;
        this.mDevice = device;
    }

    public String getCode() {
        return mResult.optString(AUTH_CODE, null);
    }

    public byte getResult() {
        return (byte) mResult.optInt(AUTH_RESULT, AbstractAppSettings.AUTH_ACCESS_DENIED);
    }

    public Device getDevice() {
        return mDevice;
    }
}
