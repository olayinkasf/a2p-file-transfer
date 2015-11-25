package android.olayinka.file.transfer.service.receive;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.olayinka.file.transfer.AppSettings;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import com.olayinka.file.transfer.A2PServer;
import com.olayinka.file.transfer.A2PServerListener;
import com.olayinka.file.transfer.R;

import java.lang.ref.WeakReference;

/**
 * Created by Olayinka on 11/8/2015.
 */
public class ReceiveService extends Service implements A2PServerListener {

    public static final int LAUNCH_SERVER = 1;
    public static final int LAUNCHED_SERVER = -1;
    public static final int STOP_SERVER = 2;
    private static final int STOPPED_SERVER = -2;

    final Messenger mMessenger = new Messenger(new IncomingHandler(this));
    NotificationCompat.Builder mNotifBuilder;
    Messenger mClient = null;
    private A2PServerTask mServer;
    private NotificationManager mNotifyMgr;

    public ReceiveService() {
    }

    private void launchServer() {
        mServer = new A2PServerTask(this);
        mServer.executeNow();
    }

    @Override
    public void onCreate() {
        mNotifBuilder = new NotificationCompat.Builder(this);
        mNotifBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("Starting file transfer service ...")
                .setTicker("Starting file transfer service ...")
                .setContentTitle(getString(R.string.app_name))
                .setOngoing(true);
        mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void exit(String code, String s, Exception e) {
        mServer = null;
        if (mClient != null) {
            try {
                Log.wtf("service", "sending " + STOPPED_SERVER);
                mClient.send(Message.obtain(null, ReceiveService.STOPPED_SERVER, 0, 0));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            mClient = null;
        }
        mNotifyMgr.cancel(AppSettings.FOREGROUND_SERVICE);
    }

    @Override
    public void message(String code, String s, Exception e) {
        switch (code) {
            case A2PServer.SERVER_INIT_SUCCESS:
                mNotifBuilder.setContentText(s);
                mNotifBuilder.setTicker(s);
                mNotifyMgr.notify(AppSettings.FOREGROUND_SERVICE, mNotifBuilder.build());
                if (mClient != null) {
                    try {
                        Log.wtf("service", "sending " + LAUNCHED_SERVER);
                        mClient.send(Message.obtain(null, ReceiveService.LAUNCHED_SERVER, 0, 0));
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                    mClient = null;
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        mNotifyMgr.cancel(AppSettings.FOREGROUND_SERVICE);
    }

    /**
     * Created by Olayinka on 11/18/2015.
     */
    public static class IncomingHandler extends Handler {
        private final WeakReference<ReceiveService> mService;

        public IncomingHandler(ReceiveService service) {
            this.mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            ReceiveService service = mService.get();
            if (service == null) return;
            Log.wtf("service", "received " + msg.what);
            switch (msg.what) {
                case LAUNCH_SERVER:
                    service.mClient = msg.replyTo;
                    if (service.mServer == null) {
                        service.launchServer();
                    }
                    break;
                case STOP_SERVER:
                    service.mClient = msg.replyTo;
                    service.mServer.exit();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}

