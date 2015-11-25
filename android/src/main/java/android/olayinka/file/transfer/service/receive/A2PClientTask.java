package android.olayinka.file.transfer.service.receive;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.olayinka.file.transfer.*;
import android.olayinka.file.transfer.Utils;
import android.os.Environment;
import android.support.v7.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.olayinka.file.transfer.*;
import com.olayinka.file.transfer.exception.WeakReferenceException;
import com.olayinka.file.transfer.model.Device;
import com.olayinka.file.transfer.model.DeviceProvider;
import com.olayinka.file.transfer.model.Transfer;
import ripped.android.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by Olayinka on 11/14/2015.
 */
public class A2PClientTask extends AsyncTask<String, String, Integer> implements A2PClient.ListenerProvider {

    final Intent mBroadcastIntent = new Intent();
    private final NotificationCompat.Builder mNotifBuilder;
    private final NotificationManager mNotifyMgr;
    private final FileTransfer.FileTransferListener mFileTransferListener = new FileTransferListenerImpl();
    A2PClient mA2PClient;
    Device mDevice;
    private WeakReference<Context> mContext;
    private final DeviceConnect.DeviceConnectListener mDeviceConnectListener = new AbstractDeviceConnectListener() {

        @Override
        protected DeviceProvider getDeviceProvider() {
            try {
                return AppSqlHelper.instance(getContext()).getDeviceProvider();
            } catch (WeakReferenceException ignored) {
            }
            return null;
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

    public A2PClientTask(Context mContext, A2PClient mA2PClient) {
        this.mContext = new WeakReference<>(mContext);
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
        mA2PClient.disconnect(null);
        return 0;
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

                LayoutInflater inflater = null;
                try {
                    inflater = LayoutInflater.from(getContext());
                } catch (WeakReferenceException e) {
                    return;
                }
                View view = inflater.inflate(R.layout.auth_code, null);

                TextView nameTextView = (TextView) view.findViewById(R.id.deviceName);
                TextView ipAddressTextView = (TextView) view.findViewById(R.id.ipAddress);
                TextView authCodeTextView = (TextView) view.findViewById(R.id.authCode);

                nameTextView.setText(name);
                ipAddressTextView.setText(ipAddress);
                authCodeTextView.setText(authCode);

                AlertDialog dialog = null;
                try {
                    dialog = new AlertDialog.Builder(getContext())
                            .setMessage("Authentication access code")
                            .setPositiveButton("DONE", null)
                            .setTitle(name)
                            .setView(view)
                            .setCancelable(false)
                            .create();
                } catch (WeakReferenceException e) {
                    return;
                }
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

    @Override
    public FileTransfer.FileTransferListener newFileTransferListener() {
        return mFileTransferListener;
    }

    @Override
    public DeviceConnect.DeviceConnectListener newDeviceConnectListener() {
        return mDeviceConnectListener;
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
            currentTransfer.setTime(System.currentTimeMillis());
        }

        @Override
        public void registerFileSize(long maxBytesAvailable) {
            currentTransfer.setExpectedSize(maxBytesAvailable);
            currentTransfer.setTransferredSize(0L);
            currentTransfer.setStatus(Transfer.Status.FINISHED);

            try {
                AppSqlHelper.instance(getContext()).getTransferProvider().insertTransfer(currentTransfer);
            } catch (WeakReferenceException ignored) {
            }
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

            try {
                getContext().sendBroadcast(mBroadcastIntent);
            } catch (WeakReferenceException ignored) {
            }
        }

        @Override
        public void registerErrorMessage(String message) {
            try {
                AppLogger.wtf(getContext(), "registerFinished", currentTransfer.getFileName() + "; " + currentTransfer.getExpectedSize() + "; " + currentTransfer.getTransferredSize());
                AppSqlHelper.instance(getContext()).getTransferProvider().updateTransfer(currentTransfer);
                getContext().sendBroadcast(new Intent("com.olayinka.file.transfer.action.REFRESH_UI"));
            } catch (WeakReferenceException ignored) {
            }
        }

        @Override
        public void registerFinished() {
            try {
                AppLogger.wtf(getContext(), "registerFinished", currentTransfer.getFileName() + "; " + currentTransfer.getExpectedSize() + "; " + currentTransfer.getTransferredSize());
                AppSqlHelper.instance(getContext()).getTransferProvider().updateTransfer(currentTransfer);
                publishProgress(AppSettings.PROGRESS_VALUE, currentTransfer.getFileName(), currentTransfer.progress() + "");
                getContext().sendBroadcast(new Intent("com.olayinka.file.transfer.action.REFRESH_UI"));
            } catch (WeakReferenceException ignored) {
            }
        }

        @Override
        public File getSaveDirectory() {
            if (Utils.isExternalStorageWritable()) {
                return new File(Environment.getExternalStorageDirectory(), "A2P File Transfer");
            }
            return new File("./");
        }
    }

    Context getContext() throws WeakReferenceException {
        Context context = mContext.get();
        if (context == null) {
            WeakReferenceException e = new WeakReferenceException("Context is missing.");
            mA2PClient.disconnect(e);
            e.printStackTrace();
            throw e;
        }
        return context;
    }
}
