package com.olayinka.file.transfer;

import com.olayinka.file.transfer.content.AbstractAppContent;
import com.olayinka.file.transfer.model.Device;
import com.olayinka.file.transfer.model.DeviceConnection;
import com.olayinka.file.transfer.model.DeviceProvider;
import ripped.android.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Olayinka on 11/14/2015.
 */
public abstract class AbstractDeviceConnectListener implements A2PClient.DeviceConnectListener {

    @Override
    public DeviceConnection registerDevice(JSONObject object) {

        //recover and remove requested authentication
        String authHash = object.optString(AbstractAppContent.DeviceColumns.AUTH_HASH, null);
        object.remove(Device.Columns.AUTH_HASH);

        Device device = Device.deviceFromJSONObject(object);

        //object to return
        object = new JSONObject();

        //Get device provider from underlying context
        DeviceProvider deviceProvider = getDeviceProvider();
        if (deviceProvider == null) {
            object.put(DeviceConnection.AUTH_RESULT, AbstractAppSettings.AUTH_ACCESS_DENIED);
            return new DeviceConnection(object, device);
        }

        //check if device is in database
        if (!(deviceProvider.updateDevice(device) || deviceProvider.insertDevice(device))) {
            throw new RuntimeException("There is an unspeakable treachery here!!");
        }

        //if device has been deauthorized, exit
        if (Device.Status.BANNED.equals(device.getStatus())) {
            object.put(DeviceConnection.AUTH_RESULT, AbstractAppSettings.AUTH_ACCESS_DENIED);
            return new DeviceConnection(object, device);
        }

        //if device is well authenticated
        if (device.getAuthHash() != null && authHash != null && authHash.equals(device.getAuthHash())) {
            object.put(DeviceConnection.AUTH_RESULT, AbstractAppSettings.AUTH_SUCCESS);
            return new DeviceConnection(object, device);
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
        deviceProvider.updateDevice(device);

        object.put(DeviceConnection.AUTH_CODE, authCode);
        object.put(DeviceConnection.AUTH_RESULT, AbstractAppSettings.AUTH_FAILED);
        return new DeviceConnection(object, device);
    }

    protected abstract DeviceProvider getDeviceProvider();

}
