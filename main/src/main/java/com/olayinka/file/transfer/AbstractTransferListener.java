package com.olayinka.file.transfer;

import com.olayinka.file.transfer.model.Device;
import com.olayinka.file.transfer.model.Transfer;
import com.olayinka.file.transfer.model.TransferProvider;

/**
 * Created by Olayinka on 11/26/2015.
 */
public abstract class AbstractTransferListener implements A2PClient.FileTransferListener {

    protected final Transfer mTransfer = new Transfer();
    private TransferProvider mTransferProvider;

    public AbstractTransferListener(Device device) {
        mTransfer.setDeviceId(device.getId());
    }

    @Override
    public void registerName(String name) {
        mTransfer.getFileName();
        mTransfer.setFileName(name);
        mTransfer.setTransferType(Transfer.TransferType.RECEIVED.toString());
        mTransfer.setTime(System.currentTimeMillis());
        mTransfer.setStatus(Transfer.Status.NO_INIT);
        mTransfer.setExpectedSize(-1L);
        mTransfer.setTransferredSize(0L);
        mTransferProvider = getTransferProvider();
        if (mTransferProvider != null) {
            mTransferProvider.insertTransfer(mTransfer);
        }
    }

    @Override
    public void registerFileSize(long fileSize) {
        mTransfer.setExpectedSize(fileSize);
        mTransfer.setTransferredSize(0L);
        mTransfer.setStatus(Transfer.Status.FAILED);
        if (mTransferProvider != null) {
            mTransferProvider.updateTransfer(mTransfer);
        }
    }

    @Override
    public void registerErrorMessage(String message) {
        if (mTransferProvider != null) {
            mTransferProvider.updateTransfer(mTransfer);
        }
    }

    @Override
    public void registerFinished() {
        if (mTransferProvider != null) {
            mTransferProvider.updateTransfer(mTransfer);
        }
    }

    @Override
    public void registerProgress(long transferred) {
        mTransfer.setTransferredSize(transferred);
    }

    public abstract TransferProvider getTransferProvider();
}
