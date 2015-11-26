package android.olayinka.file.transfer.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.olayinka.file.transfer.AppSqlHelper;
import com.olayinka.file.transfer.content.AbstractAppContent;
import com.olayinka.file.transfer.model.Device;
import com.olayinka.file.transfer.model.DeviceProvider;

/**
 * Created by Olayinka on 11/3/2015.
 */
public class SQLiteDeviceProvider implements DeviceProvider {

    private AppSqlHelper mSqlHelper;

    public SQLiteDeviceProvider(AppSqlHelper mSqlHelper) {
        this.mSqlHelper = mSqlHelper;
    }

    static public ContentValues contentValuesForDevice(Device device) {
        ContentValues contentValues = new ContentValues();
        if (device.getStatus() != null) contentValues.put(Device.Columns.STATUS, device.getStatus());
        if (device.getAuthHash() != null) contentValues.put(Device.Columns.AUTH_HASH, device.getAuthHash());
        if (device.getMacAddress() != null) contentValues.put(Device.Columns.MAC_ADDRESS, device.getMacAddress());
        if (device.getDeviceType() != null) contentValues.put(Device.Columns.DEVICE_TYPE, device.getDeviceType());
        if (device.getLastAccess() != null) contentValues.put(Device.Columns.LAST_ACCESS, device.getLastAccess());
        if (device.getLastKnownIp() != null) contentValues.put(Device.Columns.LAST_KNOWN_IP, device.getLastKnownIp());
        if (device.getName() != null) contentValues.put(Device.Columns.NAME, device.getName());
        if (device.getDisplayName() != null) contentValues.put(Device.Columns.DISPLAY_NAME, device.getDisplayName());
        if (device.getId() != null) contentValues.put(Device.Columns._ID, device.getId());
        return contentValues;
    }

    public static Device deviceFromContentValues(ContentValues values) {
        Device device = new Device();
        device.setStatus(values.getAsShort(Device.Columns.STATUS));
        device.setAuthHash(values.getAsString(Device.Columns.AUTH_HASH));
        device.setMacAddress(values.getAsString(Device.Columns.MAC_ADDRESS));
        device.setDeviceType(values.getAsString(Device.Columns.DEVICE_TYPE));
        device.setLastAccess(values.getAsLong(Device.Columns.LAST_ACCESS));
        device.setLastKnownIp(values.getAsString(Device.Columns.LAST_KNOWN_IP));
        device.setName(values.getAsString(Device.Columns.NAME));
        device.setDisplayName(values.getAsString(Device.Columns.DISPLAY_NAME));
        device.setId(values.getAsLong(Device.Columns._ID));
        return device;
    }

    @Override
    public Device findDeviceById(Long id) {
        SQLiteDatabase database = mSqlHelper.getWritableDatabase();

        String where = AbstractAppContent.DeviceColumns._ID + " = ?";
        String[] whereArgs = new String[]{id.toString()};
        Cursor cursor = database.query(Device.TABLE, null, where, whereArgs, null, null, null);

        if (cursor.getCount() != 1) {
            cursor.close();
            return null;
        }

        ContentValues values = new ContentValues();
        cursor.moveToNext();

        DatabaseUtils.cursorRowToContentValues(cursor, values);

        cursor.close();

        return deviceFromContentValues(values);
    }

    @Override
    public Device findDeviceByMacAddress(String hashId) {
        SQLiteDatabase database = mSqlHelper.getWritableDatabase();

        String where = AbstractAppContent.DeviceColumns.MAC_ADDRESS + " = ?";
        String[] whereArgs = new String[]{hashId};
        Cursor cursor = database.query(Device.TABLE, null, where, whereArgs, null, null, null);

        if (cursor.getCount() != 1) {
            cursor.close();
            return null;
        }

        ContentValues values = new ContentValues();
        cursor.moveToNext();

        DatabaseUtils.cursorRowToContentValues(cursor, values);

        return deviceFromContentValues(values);
    }

    @Override
    public void loadDeviceTransfers(Device device) {
    }

    @Override
    public boolean deleteDevice(Device device) {
        Device tmpDevice = findDeviceByMacAddress(device.getMacAddress());

        if (tmpDevice == null) return false;

        device.merge(tmpDevice);

        SQLiteDatabase database = mSqlHelper.getWritableDatabase();
        String where = AbstractAppContent.DeviceColumns._ID + " = ?";
        String[] whereArgs = new String[]{"" + device.getId()};

        database.beginTransaction();
        device.prePersist();
        device.setStatus(Device.Status.BANNED);
        int res = database.update(Device.TABLE, SQLiteDeviceProvider.contentValuesForDevice(device), where, whereArgs);
        database.setTransactionSuccessful();
        database.endTransaction();

        return res <= 1;
    }

    @Override
    public boolean insertDevice(Device device) {
        Device tmpDevice = findDeviceByMacAddress(device.getMacAddress());

        if (tmpDevice != null) return false;

        SQLiteDatabase database = mSqlHelper.getWritableDatabase();

        database.beginTransaction();
        device.prePersist();
        long id = database.insert(Device.TABLE, null, SQLiteDeviceProvider.contentValuesForDevice(device));
        database.setTransactionSuccessful();
        database.endTransaction();

        if (id <= 0) return false;

        device.setId(id);

        return true;
    }

    @Override
    public boolean updateDevice(Device device) {
        Device tmpDevice = findDeviceByMacAddress(device.getMacAddress());

        if (tmpDevice == null) return false;

        device.merge(tmpDevice);

        SQLiteDatabase database = mSqlHelper.getWritableDatabase();
        String where = AbstractAppContent.DeviceColumns._ID + " = ?";
        String[] whereArgs = new String[]{"" + device.getId()};

        database.beginTransaction();
        device.prePersist();
        int res = database.update(Device.TABLE, SQLiteDeviceProvider.contentValuesForDevice(device), where, whereArgs);
        database.setTransactionSuccessful();
        database.endTransaction();

        return res <= 1;
    }
}
