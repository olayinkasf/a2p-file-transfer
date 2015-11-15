package android.olayinka.file.transfer.service.receive;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.olayinka.file.transfer.AppLogger;
import android.olayinka.file.transfer.AppSettings;
import android.olayinka.file.transfer.AppSqlHelper;
import android.olayinka.file.transfer.AsyncTask;
import android.support.v7.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.olayinka.file.transfer.*;
import com.olayinka.file.transfer.model.Device;
import com.olayinka.file.transfer.model.DeviceProvider;
import com.olayinka.file.transfer.model.Transfer;
import ripped.android.json.JSONObject;

/**
 * Created by Olayinka on 11/14/2015.
 */
public class A2PClientTask extends AsyncTask<String, String, Integer> implements A2PClient.ListenerProvider {

    private final NotificationCompat.Builder mNotifBuilder;
    private final NotificationManager mNotifyMgr;
    A2PClient mA2PClient;
    private Context mContext;
    final Intent mBroadcastIntent = new Intent();
    Device mDevice;

    private final DeviceConnect.DeviceConnectListener mDeviceConnectListener = new AbstractDeviceConnectListener() {

        @Override
        protected DeviceProvider getDeviceProvider() {
            return AppSqlHelper.instance(mContext).getDeviceProvider();
        }

        @Override
        public Device registerDevice(JSONObject object) {
            mDevice = super.registerDevice(object);
            publishProgress(AppSettings.PROGRESS_DEVICE_REGISTERED);
            return mDevice;
        }

        @Override
        public void showAuthCode(JSONObject object) {
            publishProgress(AppSettings.PROGRESS_AUTH_CODE, object.toString());
        }
    };

    private final FileTransfer.FileTransferListener mFileTransferListener = new FileTransferListenerImpl();

    public A2PClientTask(Context mContext, A2PClient mA2PClient) {
        this.mContext = mContext;
        this.mA2PClient = mA2PClient;
        mNotifBuilder = new NotificationCompat.Builder(mContext);
        mNotifBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mNotifBuilder.setContentTitle(mContext.getString(R.string.app_name));
        mNotifBuilder.setContentText("Connected to device at " + mA2PClient.getIpAddress());
        Notification notification = mNotifBuilder.build();
        mNotifyMgr = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mA2PClient.getId(), notification);
    }

    @Override
    protected Integer doInBackground(String... params) {
        mA2PClient.run();
        return null;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        switch (values[0]) {
            case AppSettings.PROGRESS_AUTH_CODE:
                JSONObject object = new JSONObject(values[1]);
                String authCode = object.getString(Device.Columns.AUTH_HASH);
                String name = object.getString(Device.Columns.NAME);
                String ipAddress = object.getString(Device.Columns.LAST_KNOWN_IP);

                LayoutInflater inflater = LayoutInflater.from(mContext);
                View view = inflater.inflate(R.layout.auth_code, null);

                TextView nameTextView = (TextView) view.findViewById(R.id.deviceName);
                TextView ipAddressTextView = (TextView) view.findViewById(R.id.ipAddress);
                TextView authCodeTextView = (TextView) view.findViewById(R.id.authCode);

                nameTextView.setText(name);
                ipAddressTextView.setText(ipAddress);
                authCodeTextView.setText(authCode);

                AlertDialog dialog = new AlertDialog.Builder(mContext)
                        .setMessage("Authentication access code")
                        .setPositiveButton("DONE", null)
                        .setTitle(name)
                        .setView(view)
                        .setCancelable(false)
                        .create();
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                dialog.show();
                break;
            case AppSettings.PROGRESS_DEVICE_REGISTERED:
                mNotifBuilder.setContentText("Connected to " + mDevice.getDisplayName() + " at " + mDevice.getLastKnownIp());
                mNotifyMgr.notify(mA2PClient.getId(), mNotifBuilder.build());
                break;
            case AppSettings.PROGRESS_VALUE:
                mNotifBuilder.setProgress(100, Byte.valueOf(values[2]), false);
                mNotifBuilder.setContentText("Downloading " + values[1]);
                mNotifBuilder.setTicker(values[1]);
                mNotifBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("Downloading " + values[1] + " from " + mDevice.getDisplayName()));
                mNotifyMgr.notify(mA2PClient.getId(), mNotifBuilder.build());
                break;
        }
    }

    class FileTransferListenerImpl implements FileTransfer.FileTransferListener {
        Transfer currentTransfer;
        long lastProgress = System.currentTimeMillis();

        @Override
        public void registerFileName(String name) {
            currentTransfer = new Transfer();
            currentTransfer.setDevice(mDevice);
            currentTransfer.setFileName(name);
            currentTransfer.setDeviceId(mDevice.getId());
            currentTransfer.setTransferType(Transfer.TransferType.RECEIVED.toString());
        }

        @Override
        public void registerFileSize(long maxBytesAvailable) {
            currentTransfer.setExpectedSize(maxBytesAvailable);
            currentTransfer.setTransferredSize(0l);
            currentTransfer.setStatus(Transfer.Status.ACTIVE);
            AppSqlHelper.instance(mContext).getTransferProvider().insertTransfer(currentTransfer);
        }

        @Override
        public void registerProgress(long readData) {
            currentTransfer.setTransferredSize(readData);
            mBroadcastIntent.setAction("uri://com.olayinka.file.transfer/" + currentTransfer.getId());
            mBroadcastIntent.putExtra(AppSettings.PROGRESS_VALUE, currentTransfer.progress());
            if (System.currentTimeMillis() - lastProgress >= 500) {
                lastProgress = System.currentTimeMillis();
                publishProgress(AppSettings.PROGRESS_VALUE, currentTransfer.getFileName(), currentTransfer.progress() + "");
            }
            mContext.sendBroadcast(mBroadcastIntent);
        }

        @Override
        public void registerErrorMessage(String message) {
            AppLogger.wtf(mContext, "registerFinished", currentTransfer.getFileName() + "; " + currentTransfer.getExpectedSize() + "; " + currentTransfer.getTransferredSize());
            AppSqlHelper.instance(mContext).getTransferProvider().updateTransfer(currentTransfer);
        }

        @Override
        public void registerFinished() {
            AppLogger.wtf(mContext, "registerFinished", currentTransfer.getFileName() + "; " + currentTransfer.getExpectedSize() + "; " + currentTransfer.getTransferredSize());
            AppSqlHelper.instance(mContext).getTransferProvider().updateTransfer(currentTransfer);
            publishProgress(AppSettings.PROGRESS_VALUE, currentTransfer.getFileName(), currentTransfer.progress() + "");
        }
    }

    @Override
    public FileTransfer.FileTransferListener newFileTransferListener() {
        return mFileTransferListener;
    }

    @Override
    public DeviceConnect.DeviceConnectListener newDeviceConnectListener() {
        return mDeviceConnectListener;
    }
}
