package android.olayinka.file.transfer.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.olayinka.file.transfer.AppSqlHelper;
import com.olayinka.file.transfer.content.AbstractAppContent;
import com.olayinka.file.transfer.model.Device;
import com.olayinka.file.transfer.model.Transfer;
import com.olayinka.file.transfer.model.TransferProvider;

/**
 * Created by Olayinka on 11/15/2015.
 */
public class SQLiteTransferProvider implements TransferProvider {

    private AppSqlHelper mSqlHelper;

    public SQLiteTransferProvider(AppSqlHelper mSqlHelper) {
        this.mSqlHelper = mSqlHelper;
    }

    @Override
    public Transfer findTransferById(Long id) {
        if (id == null) return null;
        SQLiteDatabase database = mSqlHelper.getWritableDatabase();

        String where = AbstractAppContent.TransferColumns._ID + " = ?";
        String[] whereArgs = new String[]{id.toString()};
        Cursor cursor = database.query(Transfer.TABLE, null, where, whereArgs, null, null, null);

        if (cursor.getCount() != 1) {
            cursor.close();
            return null;
        }

        ContentValues values = new ContentValues();
        cursor.moveToNext();

        DatabaseUtils.cursorRowToContentValues(cursor, values);

        return transferFromContentValues(values);
    }

    public static Transfer transferFromContentValues(ContentValues values) {
        Transfer transfer = new Transfer();
        transfer.setId(values.getAsLong(Transfer.Columns._ID));
        transfer.setDeviceId(values.getAsLong(Transfer.Columns.DEVICE_ID));
        transfer.setFileName(values.getAsString(Transfer.Columns.FILE_NAME));
        transfer.setExpectedSize(values.getAsLong(Transfer.Columns.EXPECTED_SIZE));
        transfer.setTransferredSize(values.getAsLong(Transfer.Columns.TRANSFERRED_SIZE));
        transfer.setTransferType(values.getAsString(Transfer.Columns.TRANSFER_TYPE));
        transfer.setTime(values.getAsLong(Transfer.Columns.TIME));
        transfer.setStatus(values.getAsShort(Transfer.Columns.STATUS));
        return transfer;
    }

    @Override
    public boolean archiveTransfer(Transfer transfer) {
        Transfer tmpTransfer = findTransferById(transfer.getId());

        if (tmpTransfer == null) return false;

        transfer.merge(tmpTransfer);

        SQLiteDatabase database = mSqlHelper.getWritableDatabase();
        String where = AbstractAppContent.TransferColumns._ID + " = ?";
        String[] whereArgs = new String[]{"" + transfer.getId()};

        database.beginTransaction();
        transfer.setStatus(Transfer.Status.ARCHIVED);
        int res = database.update(Transfer.TABLE, contentValuesForTransfer(transfer), where, whereArgs);
        database.setTransactionSuccessful();
        database.endTransaction();

        return res <= 1;
    }

    public static ContentValues contentValuesForTransfer(Transfer transfer) {
        ContentValues contentValues = new ContentValues();
        if (transfer.getId() != null) contentValues.put(Transfer.Columns._ID, transfer.getId());
        if (transfer.getDeviceId() != null) contentValues.put(Transfer.Columns.DEVICE_ID, transfer.getDeviceId());
        if (transfer.getFileName() != null) contentValues.put(Transfer.Columns.FILE_NAME, transfer.getFileName());
        if (transfer.getExpectedSize() != null)
            contentValues.put(Transfer.Columns.EXPECTED_SIZE, transfer.getExpectedSize());
        if (transfer.getTransferredSize() != null)
            contentValues.put(Transfer.Columns.TRANSFERRED_SIZE, transfer.getTransferredSize());
        if (transfer.getTransferType() != null)
            contentValues.put(Transfer.Columns.TRANSFER_TYPE, transfer.getTransferType());
        if (transfer.getTime() != null) contentValues.put(Transfer.Columns.TIME, transfer.getTime());
        if (transfer.getStatus() != null) contentValues.put(Transfer.Columns.STATUS, transfer.getStatus());
        return contentValues;
    }

    @Override
    public boolean insertTransfer(Transfer transfer) {
        Transfer tmpTransfer = findTransferById(transfer.getId());

        if (tmpTransfer != null) return false;

        SQLiteDatabase database = mSqlHelper.getWritableDatabase();

        database.beginTransaction();
        long id = database.insert(Transfer.TABLE, null, SQLiteTransferProvider.contentValuesForTransfer(transfer));
        database.setTransactionSuccessful();
        database.endTransaction();

        if (id <= 0) return false;

        transfer.setId(id);

        return true;
    }

    @Override
    public boolean updateTransfer(Transfer transfer) {
        Transfer tmpTransfer = findTransferById(transfer.getId());

        if (tmpTransfer == null) return false;

        transfer.merge(tmpTransfer);

        SQLiteDatabase database = mSqlHelper.getWritableDatabase();
        String where = Transfer.Columns._ID + " = ?";
        String[] whereArgs = new String[]{"" + transfer.getId()};

        database.beginTransaction();
        int res = database.update(Device.TABLE, contentValuesForTransfer(transfer), where, whereArgs);
        database.setTransactionSuccessful();
        database.endTransaction();

        return res <= 1;
    }
}
