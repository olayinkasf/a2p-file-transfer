package android.olayinka.file.transfer.service.receive;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.olayinka.file.transfer.AppSettings;
import android.os.*;
import android.support.v7.app.NotificationCompat;
import com.olayinka.file.transfer.A2PServer;
import com.olayinka.file.transfer.A2PServerListener;
import com.olayinka.file.transfer.R;

/**
 * Created by Olayinka on 11/8/2015.
 */
public class ReceiveService extends Service implements A2PServerListener {

    public static final String NAME = "Receive Service";
    public static final int LAUNCHED_SERVER = 2;
    private static final int STOPPED_SERVER = 3;
    public static final int STOP_SERVER = 4;
    private A2PServerTask mServer;
    NotificationCompat.Builder mNotifBuilder;
    private NotificationManager mNotifyMgr;

    Messenger mClient = null;
    int mValue = 0;
    public static final int LAUNCH_SERVER = 1;

    @SuppressLint("HandlerLeak")
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LAUNCH_SERVER:
                    mClient = msg.replyTo;
                    if (mServer == null) {
                        launchServer();
                    }
                    break;
                case STOP_SERVER:
                    mClient = msg.replyTo;
                    mServer.disconnect();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void launchServer() {
        mServer = new A2PServerTask(this);
        mServer.executeNow();
    }

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    public void onCreate() {
        showNotification();
    }

    public ReceiveService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    protected void showNotification() {
        mNotifBuilder = new NotificationCompat.Builder(this);
        mNotifBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("Starting file transfer service ...")
                .setTicker("Starting file transfer service ...")
                .setContentTitle(getString(R.string.app_name))
                .setOngoing(true);
        Notification notification = mNotifBuilder.build();
        mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(AppSettings.FOREGROUND_SERVICE, notification);
    }

    @Override
    public void exit(String code, String s, Exception e) {
        mServer = null;
        try {
            mClient.send(Message.obtain(null, ReceiveService.STOPPED_SERVER, 0, 0));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            mClient = null;
        }
        mClient = null;
        mNotifyMgr.cancel(AppSettings.FOREGROUND_SERVICE);
        stopSelf();
    }

    @Override
    public void message(String code, String s, Exception e) {
        switch (code) {
            case A2PServer.SERVER_INIT_SUCCESS:
                mNotifBuilder.setContentText(s);
                mNotifBuilder.setTicker(s);
                mNotifyMgr.notify(AppSettings.FOREGROUND_SERVICE, mNotifBuilder.build());
                try {
                    mClient.send(Message.obtain(null, ReceiveService.LAUNCHED_SERVER, 0, 0));
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    mClient = null;
                }
                break;
        }
    }


    @Override
    public void onDestroy() {
        mNotifyMgr.cancel(AppSettings.FOREGROUND_SERVICE);
    }

}

