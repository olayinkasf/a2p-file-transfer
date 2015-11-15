package com.olayinka.file.transfer;

import com.olayinka.file.transfer.content.AbstractAppContent;
import com.olayinka.file.transfer.model.Device;
import com.olayinka.file.transfer.model.DeviceProvider;
import ripped.android.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Olayinka on 11/14/2015.
 */
public abstract class AbstractDeviceConnectListener implements DeviceConnect.DeviceConnectListener {

    @Override
    public Device registerDevice(JSONObject object) {

        String authHash = object.optString(AbstractAppContent.DeviceColumns.AUTH_HASH, null);
        object.remove(Device.Columns.AUTH_HASH);
        Device device = Device.deviceFromJSONObject(object);


        //check if device is in database
        DeviceProvider mDeviceProvider = getDeviceProvider();
        if (!(mDeviceProvider.updateDevice(device) || mDeviceProvider.insertDevice(device))) {
            throw new RuntimeException("There is an unspeakable treachery here!!");
        }

        //if device has been deauthorized, exit
        if (Device.Status.BANNED.equals(device.getStatus())) {
            object.put(AbstractAppSettings.AUTH_RESULT, AbstractAppSettings.AUTH_ACCESS_DENIED);
            return device;
        }

        //if device is well authenticated
        if (device.getAuthHash() != null && authHash != null && authHash.equals(device.getAuthHash())) {
            object.put(AbstractAppSettings.AUTH_RESULT, AbstractAppSettings.AUTH_SUCCESS);
            return device;
        }

        //generate authentication code and persist
        String authCode = "";
        int nxt = Utils.randomSize(0, 10);
        authCode += "0123456789".substring(nxt, nxt + 1);
        nxt = Utils.randomSize(0, 10);
        authCode += "0123456789".substring(nxt, nxt + 1);
        nxt = Utils.randomSize(0, 10);
        authCode += "0123456789".substring(nxt, nxt + 1);
        nxt = Utils.randomSize(0, 10);
        authCode += "0123456789".substring(nxt, nxt + 1);

        try {
            authHash = Utils.hash(authCode);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

        device.setAuthHash(authHash);
        mDeviceProvider.updateDevice(device);

        object.put(Device.Columns.AUTH_HASH, authCode);
        object.put(AbstractAppSettings.AUTH_RESULT, AbstractAppSettings.AUTH_FAILED);
        return device;
    }

    protected abstract DeviceProvider getDeviceProvider();

}
