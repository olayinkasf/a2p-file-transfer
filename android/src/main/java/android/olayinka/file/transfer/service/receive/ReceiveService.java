package android.olayinka.file.transfer.service.receive;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.olayinka.file.transfer.AppSettings;
import android.support.v7.app.NotificationCompat;
import com.olayinka.file.transfer.A2PServer;
import com.olayinka.file.transfer.A2PServerListener;
import com.olayinka.file.transfer.R;

/**
 * Created by Olayinka on 11/8/2015.
 */
public class ReceiveService extends IntentService implements A2PServerListener {

    public static final String NAME = "Receive Service";
    private A2PServerTask mServer;
    NotificationCompat.Builder mNotifBuilder;
    private NotificationManager mNotifyMgr;

    public ReceiveService() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mNotifBuilder = new NotificationCompat.Builder(this);
        mNotifBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("Starting file transfer service ...")
                .setContentTitle(getString(R.string.app_name))
                .setOngoing(true);
        Notification notification = mNotifBuilder.build();
        mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(AppSettings.FOREGROUND_RECEIVE, notification);
        mServer = new A2PServerTask(this);
        mServer.executeNow();
    }

    @Override
    public void exit(String code, String s, Exception e) {
        mServer = null;
        stopSelf();
    }

    @Override
    public void message(String code, String s, Exception e) {
        switch (code) {
            case A2PServer.SERVER_INIT_SUCCESS:
                mNotifBuilder.setContentText(s);
                mNotifyMgr.notify(AppSettings.FOREGROUND_RECEIVE, mNotifBuilder.build());
                break;
        }
    }
}

