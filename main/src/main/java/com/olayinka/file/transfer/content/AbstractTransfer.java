package com.olayinka.file.transfer.content;

/**
 * Created by Olayinka on 11/15/2015.
 */
public class AbstractTransfer {
    public static final String TABLE = "transfer";


    public enum TransferType {
        SENT, RECEIVED
    }

    public static class Status {
        public static final Short ACTIVE = 0;
        public static final Short ARCHIVED = 1;
    }

    public static class Columns extends AbstractAppContent.TransferColumns {
    }
}