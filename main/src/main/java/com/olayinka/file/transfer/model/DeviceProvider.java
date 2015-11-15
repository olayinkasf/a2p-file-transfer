package com.olayinka.file.transfer.model;

/**
 * Created by Olayinka on 11/2/2015.
 */
public interface DeviceProvider {

    Device findDeviceById(Long id);

    Device findDeviceByMacAddress(String hashId);

    void loadDeviceTransfers(Device device);

    boolean deleteDevice(Device device);

    boolean insertDevice(Device device);

    boolean updateDevice(Device device);
}
