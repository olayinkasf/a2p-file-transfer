package com.olayinka.file.transfer.model;

import com.olayinka.file.transfer.content.AbstractTransfer;

/**
 * Created by Olayinka on 11/2/2015.
 */
public class Transfer extends AbstractTransfer {
    private Long id;
    private Long deviceId;
    private String transferType;
    private String fileName;
    private Long expectedSize;
    private Long transferredSize;
    private Long time;
    private Device device;
    private Short status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public String getTransferType() {
        return transferType;
    }

    public void setTransferType(String transferType) {
        this.transferType = transferType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getExpectedSize() {
        return expectedSize;
    }

    public void setExpectedSize(Long expectedSize) {
        this.expectedSize = expectedSize;
    }

    public Long getTransferredSize() {
        return transferredSize;
    }

    public void setTransferredSize(Long transferredSize) {
        this.transferredSize = transferredSize;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transfer transfer = (Transfer) o;

        if (id != null ? !id.equals(transfer.id) : transfer.id != null) return false;
        if (deviceId != null ? !deviceId.equals(transfer.deviceId) : transfer.deviceId != null) return false;
        if (transferType != null ? !transferType.equals(transfer.transferType) : transfer.transferType != null)
            return false;
        if (fileName != null ? !fileName.equals(transfer.fileName) : transfer.fileName != null) return false;
        if (expectedSize != null ? !expectedSize.equals(transfer.expectedSize) : transfer.expectedSize != null)
            return false;
        if (transferredSize != null ? !transferredSize.equals(transfer.transferredSize) : transfer.transferredSize != null)
            return false;
        if (time != null ? !time.equals(transfer.time) : transfer.time != null) return false;
        return !(status != null ? !status.equals(transfer.status) : transfer.status != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (deviceId != null ? deviceId.hashCode() : 0);
        result = 31 * result + (transferType != null ? transferType.hashCode() : 0);
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        result = 31 * result + (expectedSize != null ? expectedSize.hashCode() : 0);
        result = 31 * result + (transferredSize != null ? transferredSize.hashCode() : 0);
        result = 31 * result + (time != null ? time.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        if (device.getId() == deviceId) throw new RuntimeException("This device doesn't beLong here.");
        this.device = device;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public Short getStatus() {
        return status;
    }

    public void merge(Transfer tmpTransfer) {
        if (tmpTransfer.id != null && status == null) id = tmpTransfer.id;
        if (tmpTransfer.deviceId != null && id == null) deviceId = tmpTransfer.deviceId;
        if (tmpTransfer.fileName != null && fileName == null) fileName = tmpTransfer.fileName;
        if (tmpTransfer.expectedSize != null && expectedSize == null) expectedSize = tmpTransfer.expectedSize;
        if (tmpTransfer.transferredSize != null && transferredSize == null)
            transferredSize = tmpTransfer.transferredSize;
        if (tmpTransfer.transferType != null && transferType == null) transferType = tmpTransfer.transferType;
        if (tmpTransfer.time != null && time == null) time = tmpTransfer.time;
        if (tmpTransfer.status != null && status == null) status = tmpTransfer.status;
    }

    public byte progress() {
        return (byte) ((transferredSize * 100) / expectedSize);
    }
}
