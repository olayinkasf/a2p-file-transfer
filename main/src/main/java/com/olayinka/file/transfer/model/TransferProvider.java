package com.olayinka.file.transfer.model;

/**
 * Created by Olayinka on 11/2/2015.
 */
public interface TransferProvider {

    Transfer findTransferById(Long id);

    boolean archiveTransfer(Transfer transfer);

    boolean insertTransfer(Transfer transfer);

    boolean updateTransfer(Transfer transfer);
}
