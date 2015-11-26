package android.olayinka.file.transfer.service.receive;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.olayinka.file.transfer.*;
import android.os.Environment;
import android.support.v7.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.olayinka.file.transfer.A2PClient;
import com.olayinka.file.transfer.AbstractDeviceConnectListener;
import com.olayinka.file.transfer.AbstractTransferListener;
import com.olayinka.file.transfer.R;
import com.olayinka.file.transfer.exception.WeakReferenceException;
import com.olayinka.file.transfer.model.Device;
import com.olayinka.file.transfer.model.DeviceConnection;
import com.olayinka.file.transfer.model.DeviceProvider;
import com.olayinka.file.transfer.model.TransferProvider;
import ripped.android.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by Olayinka on 11/14/2015.
 */
public class A2PClientTask extends AsyncTask<String, Object, Integer> implements A2PClient.ListenerProvider {

    final Intent mBroadcastIntent = new Intent();
    private final NotificationCompat.Builder mNotifBuilder;
    private final NotificationManager mNotifyMgr;
    A2PClient mA2PClient;
    private WeakReference<Context> mContext;
    private DeviceConnection mConnection = null;
    private final A2PClient.DeviceConnectListener mDeviceConnectListener = new AbstractDeviceConnectListener() {

        @Override
        protected DeviceProvider getDeviceProvider() {
            try {
                return AppSqlHelper.instance(getContext()).getDeviceProvider();
            } catch (WeakReferenceException ignored) {
            }
            return null;
        }


        @Override
        public DeviceConnection registerDevice(JSONObject object) {
            DeviceConnection connection = super.registerDevice(object);
            if (connection.getResult() == AppSettings.AUTH_SUCCESS) {
                mConnection = connection;
                publishProgress(AppSettings.PROGRESS_DEVICE_REGISTERED);
            }
            return connection;
        }

        @Override
        public void showAuthCode(DeviceConnection connection) {
            publishProgress(AppSettings.PROGRESS_AUTH_CODE, connection);
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
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
        switch ((String) values[0]) {
            case AppSettings.PROGRESS_AUTH_CODE:
                DeviceConnection connection = (DeviceConnection) values[1];
                String authCode = connection.getCode();
                String name = connection.getDevice().getDisplayName();
                String ipAddress = connection.getDevice().getLastKnownIp();

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
                mNotifBuilder.setContentText("Connected to " + mConnection.getDevice().getDisplayName() + " at " + mConnection.getDevice().getLastKnownIp());
                mNotifyMgr.notify(mA2PClient.getId(), mNotifBuilder.build());
                break;
            case AppSettings.PROGRESS_VALUE:
                byte progress = (byte) values[2];
                if (progress < 100) {
                    mNotifBuilder.setProgress(100, progress, false);
                    mNotifBuilder.setContentText("Downloading " + values[1]);
                } else if (progress == 100) {
                    mNotifBuilder.setContentText("Downloaded " + values[1]);
                    mNotifBuilder.setProgress(0, 0, false);
                }
                mNotifBuilder.setTicker((String) values[1]);
                mNotifBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("Downloading " + values[1] + " from " + mConnection.getDevice().getDisplayName()));
                mNotifyMgr.notify(mA2PClient.getId(), mNotifBuilder.build());
                break;
        }
    }

    @Override
    public A2PClient.FileTransferListener newFileTransferListener(Device device) {
        return new AndroidTransferListener();
    }

    @Override
    public A2PClient.DeviceConnectListener deviceConnectListener() {
        return mDeviceConnectListener;
    }

    class AndroidTransferListener extends AbstractTransferListener {

        long lastProgress = System.currentTimeMillis();

        public AndroidTransferListener() {
            super(mConnection.getDevice());
        }

        @Override
        public void registerProgress(long readData) {
            super.registerProgress(readData);
            mBroadcastIntent.setAction("a2p://com.olayinka.file.transfer/" + mTransfer.getId());
            mBroadcastIntent.putExtra(AppSettings.PROGRESS_VALUE, mTransfer.progress());
            if (System.currentTimeMillis() - lastProgress >= 500) {
                lastProgress = System.currentTimeMillis();
                publishProgress(AppSettings.PROGRESS_VALUE, mTransfer.getFile().getName(), mTransfer.progress());
            }

            try {
                getContext().sendBroadcast(mBroadcastIntent);
            } catch (WeakReferenceException ignored) {
            }
        }

        @Override
        public TransferProvider getTransferProvider() {
            try {
                return AppSqlHelper.instance(getContext()).getTransferProvider();
            } catch (WeakReferenceException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void registerErrorMessage(String message) {
            super.registerErrorMessage(message);
            try {
                AppLogger.wtf(getContext(), "registerFinished", mTransfer.getFileName() + "; " + mTransfer.getExpectedSize() + "; " + mTransfer.getTransferredSize());
                getContext().sendBroadcast(new Intent("com.olayinka.file.transfer.action.REFRESH_UI"));
            } catch (WeakReferenceException ignored) {
            }
        }

        @Override
        public void registerFinished() {
            super.registerFinished();
            try {
                AppLogger.wtf(getContext(), "registerFinished", mTransfer.getFileName() + "; " + mTransfer.getExpectedSize() + "; " + mTransfer.getTransferredSize());
                publishProgress(AppSettings.PROGRESS_VALUE, mTransfer.getFileName(), mTransfer.progress());
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
