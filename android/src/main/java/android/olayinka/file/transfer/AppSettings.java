package android.olayinka.file.transfer;

import com.olayinka.file.transfer.AbstractAppSettings;
import com.olayinka.file.transfer.content.AbstractDevice;

/**
 * Created by Olayinka on 10/17/2015.
 */
public class AppSettings extends AbstractAppSettings {

    public static final int FOREGROUND_SEND = 0x87654321;
    public static final int FOREGROUND_SERVICE = 0x98765432;

    public static final int NOTIF_INIT = 0x12345678;
    public static final int NOTIF_CONNECT_SUCCESS = NOTIF_INIT;
    public static final int NOTIF_REQUEST_AUTH = NOTIF_CONNECT_SUCCESS;
    public static final int NOTIF_AUTH_SUCCESS = NOTIF_REQUEST_AUTH;
    public static final int NOTIF_FILE_PROGRESS = NOTIF_AUTH_SUCCESS;
    public static final int NOTIF_FILE_ERROR = 0x23456789;
    public static final String TEXT = ".text";
    public static final String APP_SETTINGS = "app.settings";
    public static final String LOG_APP_ACTIVITY = "log.app.activity";
    public static final String PROGRESS_VALUE = "progress.value";

    public static final String WIFI_AP_STATE_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
    public static final String LAST_ENCODED = "last.encoded";

    public static AbstractDevice.DeviceType DEVICE_TYPE = AbstractDevice.DeviceType.MOBILE;
}
