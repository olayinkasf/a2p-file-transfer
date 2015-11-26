package com.olayinka.file.transfer.model;

import com.olayinka.file.transfer.content.AbstractDevice;
import ripped.android.json.JSONObject;

import java.util.*;

/**
 * Created by Olayinka on 11/2/2015.
 */
public class Device extends AbstractDevice {
    private Long id;
    private String name;
    private String macAddress;
    private String deviceType;
    private String displayName;
    private String authHash;
    private String lastKnownIp;
    private Short status;
    private Long lastAccess;

    private List<Transfer> transfers;

    public Device() {
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getAuthHash() {
        return authHash;
    }

    public void setAuthHash(String authHash) {
        this.authHash = authHash;
    }

    public String getLastKnownIp() {
        return lastKnownIp;
    }

    public void setLastKnownIp(String lastKnownIp) {
        this.lastKnownIp = lastKnownIp;
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public Long getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(Long lastAccess) {
        this.lastAccess = lastAccess;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Device device = (Device) o;

        if (id != null ? !id.equals(device.id) : device.id != null) return false;
        if (name != null ? !name.equals(device.name) : device.name != null) return false;
        if (macAddress != null ? !macAddress.equals(device.macAddress) : device.macAddress != null) return false;
        if (deviceType != null ? !deviceType.equals(device.deviceType) : device.deviceType != null) return false;
        if (authHash != null ? !authHash.equals(device.authHash) : device.authHash != null) return false;
        if (lastKnownIp != null ? !lastKnownIp.equals(device.lastKnownIp) : device.lastKnownIp != null) return false;
        if (status != null ? !status.equals(device.status) : device.status != null) return false;
        if (lastAccess != null ? !lastAccess.equals(device.lastAccess) : device.lastAccess != null) return false;
        return !(transfers != null ? !transfers.equals(device.transfers) : device.transfers != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (macAddress != null ? macAddress.hashCode() : 0);
        result = 31 * result + (deviceType != null ? deviceType.hashCode() : 0);
        result = 31 * result + (authHash != null ? authHash.hashCode() : 0);
        result = 31 * result + (lastKnownIp != null ? lastKnownIp.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (lastAccess != null ? lastAccess.hashCode() : 0);
        result = 31 * result + (transfers != null ? transfers.hashCode() : 0);
        return result;
    }

    public List<Transfer> getTransfers() {
        return transfers;
    }

    public void setTransfers(List<Transfer> transfers) {
        this.transfers = transfers;
    }

    @Override
    public String toString() {
        JSONObject object = new JSONObject();
        object.put(Columns.NAME, name);
        object.put(Columns.AUTH_HASH, authHash);
        object.put(Columns.LAST_KNOWN_IP, lastKnownIp);
        object.put(Columns.STATUS, status);
        object.put(Columns._ID, id);
        object.put(Columns.MAC_ADDRESS, macAddress);
        object.put(Columns.DEVICE_TYPE, deviceType);
        object.put(Columns.LAST_ACCESS, lastAccess);
        return object.toString();
    }

    public static Device deviceFromJSONObject(JSONObject object) {
        Device device = new Device();
        Iterator<String> iter = object.keys();
        while (iter.hasNext()) {
            String key = iter.next();

            Object value = object.get(key);
            switch (key) {
                case Device.Columns.STATUS:
                    device.status = Short.valueOf(value.toString());
                    break;
                case Device.Columns.AUTH_HASH:
                    device.authHash = (String) value;
                    break;
                case Device.Columns.MAC_ADDRESS:
                    device.macAddress = (String) value;
                    break;
                case Device.Columns.DEVICE_TYPE:
                    device.deviceType = (String) value;
                    break;
                case Device.Columns.LAST_ACCESS:
                    device.lastAccess = Long.valueOf(value.toString());
                    break;
                case Device.Columns.LAST_KNOWN_IP:
                    device.lastKnownIp = (String) value;
                    break;
                case Device.Columns.NAME:
                    device.name = (String) value;
                    break;
            }
        }

        return device;
    }

    public void merge(Device tmpDevice) {
        if (tmpDevice.status != null && status == null) status = tmpDevice.status;
        if (tmpDevice.id != null && id == null) id = tmpDevice.id;
        if (tmpDevice.authHash != null && authHash == null) authHash = tmpDevice.authHash;
        if (tmpDevice.macAddress != null && macAddress == null) macAddress = tmpDevice.macAddress;
        if (tmpDevice.deviceType != null && deviceType == null) deviceType = tmpDevice.deviceType;
        if (tmpDevice.lastAccess != null && lastAccess == null) lastAccess = tmpDevice.lastAccess;
        if (tmpDevice.lastKnownIp != null && lastKnownIp == null) lastKnownIp = tmpDevice.lastKnownIp;
        if (tmpDevice.name != null && name == null) name = tmpDevice.name;
        if (tmpDevice.displayName != null && displayName == null) displayName = tmpDevice.displayName;
    }

    public void prePersist() {
        lastAccess = System.currentTimeMillis();
        if (status == null) status = Status.ACTIVE;
        if (displayName == null) displayName = getName();
    }
}
