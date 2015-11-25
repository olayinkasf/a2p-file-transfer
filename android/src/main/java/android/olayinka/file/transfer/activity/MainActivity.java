/*
 * Copyright 2015
 *
 *     Olayinka S. Folorunso <mail@olayinkasf.com>
 *     http://olayinkasf.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.olayinka.file.transfer.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.*;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.olayinka.file.transfer.AppSettings;
import android.olayinka.file.transfer.AppSqlHelper;
import android.olayinka.file.transfer.Utils;
import android.olayinka.file.transfer.adapter.TransferAdapter;
import android.olayinka.file.transfer.model.SQLiteDeviceProvider;
import android.olayinka.file.transfer.service.NetworkStateBroadcastReceiver;
import android.olayinka.file.transfer.service.receive.ReceiveService;
import android.olayinka.file.transfer.service.send.SendService;
import android.olayinka.file.transfer.widget.ServiceButton;
import android.os.*;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.olayinka.file.transfer.R;
import com.olayinka.file.transfer.model.Device;
import com.olayinka.file.transfer.model.DeviceProvider;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Olayinka on 8/8/2015.
 */
public class MainActivity extends AppCompatActivity implements ServiceConnection {
    public static final int DEVICE_REQUEST_CODE = 0x4856;
    public static final int PICK_FILE_REQUEST_CODE = 0x4152;
    View.OnClickListener mSendButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            selectFile();
        }
    };
    ListView mLogListView;
    Messenger mMessenger = new Messenger(new IncomingHandler(this));
    Uri[] mWaitingUris;
    private DeviceProvider mDeviceProvider;
    private NetworkStateBroadcastReceiver mBroadcastReceiver;
    private BroadcastReceiver mRefreshUiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mLogListView.setAdapter(new TransferAdapter(context));
        }
    };
    private ServiceButton mServiceButton;
    private String mConnectionState = NetworkStateBroadcastReceiver.STATE_IMPOSSIBLE;
    private ProgressDialog mProgressDialog;
    private boolean mProgressShown = false;
    private String mProgressMessage;
    private boolean mBound;
    private Messenger mSendServiceMessenger;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.deviceInfo) {
            startActivity(new Intent(this, QRCodeActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        findViewById(R.id.sendFile).setOnClickListener(mSendButtonClickListener);
        mLogListView = (ListView) findViewById(R.id.list);
        mLogListView.setAdapter(new TransferAdapter(this));
        mDeviceProvider = AppSqlHelper.instance(this).getDeviceProvider();
        mBroadcastReceiver = new NetworkStateBroadcastReceiver(this) {
            @Override
            public void publishState(String state) {
                mConnectionState = state;
                if (mServiceButton == null) return;
                switch (state) {
                    case STATE_POSSIBLE_WIFI:
                        if (mConnectionState.equals(SendService.STATE_CONNECTED)) {
                            mServiceButton.setState(SendService.STATE_CONNECTED);
                        } else {
                            mServiceButton.setState(STATE_POSSIBLE_WIFI);
                        }
                        break;
                    case STATE_POSSIBLE_HOTSPOT:
                        if (mConnectionState.equals(SendService.STATE_CONNECTED)) {
                            mServiceButton.setState(SendService.STATE_CONNECTED);
                        } else {
                            mServiceButton.setState(STATE_POSSIBLE_HOTSPOT);
                        }
                        break;
                    case STATE_IMPOSSIBLE_WIFI:
                        mServiceButton.setState(STATE_IMPOSSIBLE_WIFI);
                        break;
                    case STATE_IMPOSSIBLE:
                        mServiceButton.setState(STATE_IMPOSSIBLE);
                        break;
                }
            }
        };
        initToolbar();
        registerReceiver();
        // Bind to the service
        bindService();
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(AppSettings.WIFI_AP_STATE_ACTION);
        registerReceiver(mBroadcastReceiver, filter);
        registerReceiver(mRefreshUiReceiver, new IntentFilter("com.olayinka.file.transfer.action.REFRESH_UI"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mProgressShown) {
            startConnectionProgress(mProgressMessage);
        }
    }

    private void bindService() {
        bindService(new Intent(this, ReceiveService.class), this, Context.BIND_AUTO_CREATE);
    }

    void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mServiceButton = (ServiceButton) toolbar.findViewById(R.id.serviceButton);
        mServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mConnectionState) {
                    case NetworkStateBroadcastReceiver.STATE_POSSIBLE_WIFI:
                        Utils.toast(v.getContext(), "A connection will be established on your wifi network.");
                        startConnectionProgress("Wait while a connection is established on your wifi network.");
                        break;
                    case NetworkStateBroadcastReceiver.STATE_POSSIBLE_HOTSPOT:
                        Utils.toast(v.getContext(), "A connection will be established on your mobile hotspot.");
                        startConnectionProgress("Wait while a connection is established on your mobile hotspot.");
                        break;
                    case NetworkStateBroadcastReceiver.STATE_IMPOSSIBLE_WIFI:
                        Utils.toast(v.getContext(), "Please connect to a wifi network");
                        break;
                    case NetworkStateBroadcastReceiver.STATE_IMPOSSIBLE:
                        Utils.toast(v.getContext(), "Please connect to a wifi network or start a mobile hotspot");
                        break;
                    case SendService.STATE_CONNECTED:
                        Message message = Message.obtain(null, ReceiveService.STOP_SERVER, 0, 0);
                        message.replyTo = mMessenger;
                        try {
                            Log.wtf("activity", "sending " + message.what);
                            mSendServiceMessenger.send(message);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        mConnectionState = NetworkStateBroadcastReceiver.STATE_IMPOSSIBLE;
                        unregisterReceiver(mBroadcastReceiver);
                        registerReceiver();
                        break;
                }
            }
        });
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.app_name);
    }

    private void startConnectionProgress(String messageText) {
        mProgressMessage = messageText;
        mProgressShown = false;
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.setMessage(messageText);
        else mProgressDialog = ProgressDialog.show(this, null, mProgressMessage);
        boolean launched = false;
        while (!launched) {
            try {
                Message message = Message.obtain(null, ReceiveService.LAUNCH_SERVER, 0, 0);
                message.replyTo = mMessenger;
                Log.wtf("activity", "sending " + message.what);
                mSendServiceMessenger.send(message);
                launched = true;
            } catch (Throwable e) {
                e.printStackTrace();
                bindService();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            mWaitingUris = null;
            if (intent.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, true)) {
                if (Utils.hasJellyBean()) {
                    ClipData clip = intent.getClipData();
                    if (clip != null) {
                        mWaitingUris = new Uri[clip.getItemCount()];
                        for (int i = 0; i < clip.getItemCount(); i++) {
                            Uri uri = clip.getItemAt(i).getUri();
                            mWaitingUris[i] = uri;
                        }
                    }
                } else {
                    ArrayList<String> paths = intent.getStringArrayListExtra(FilePickerActivity.EXTRA_PATHS);
                    if (paths != null) {
                        mWaitingUris = new Uri[paths.size()];
                        for (int i = 0; i < paths.size(); i++) {
                            Uri uri = Uri.parse(paths.get(i));
                            mWaitingUris[i] = (uri);
                        }
                    }
                }
            } else {
                mWaitingUris = new Uri[1];
                Uri uri = intent.getData();
                mWaitingUris[0] = uri;
            }
            if (mWaitingUris == null || mWaitingUris.length == 0) {
                Utils.toast(this, "No file selected!");
            } else {
                startActivityForResult(new Intent(MainActivity.this, SelectDeviceActivity.class), DEVICE_REQUEST_CODE);
            }
        } else if (resultCode == RESULT_OK && requestCode == DEVICE_REQUEST_CODE) {
            Device device = mDeviceProvider.findDeviceById(intent.getLongExtra(SelectDeviceActivity.SELECTED_DEVICE_ID, 0L));
            if (device == null) {
                Utils.toast(this, getString(R.string.error_selecting_device));
            } else {
                connectToDevice(SQLiteDeviceProvider.contentValuesForDevice(device));
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void connectToDevice(ContentValues device) {
        Intent intent = new Intent(getApplicationContext(), SendService.class);
        intent.putExtra(SendService.DEVICE, device);
        intent.putExtra(SendService.DATA, mWaitingUris);
        mWaitingUris = null;
        startService(intent);
        Utils.toast(this, "Check the notification panel for transfer progress");
    }

    private void selectFile() {
        Intent i = new Intent(this, FilePickerActivity.class);

        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, true);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);

        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

        startActivityForResult(i, PICK_FILE_REQUEST_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            unbindService(this);
            mBound = false;
        }
        unregisterReceiver(mBroadcastReceiver);
        unregisterReceiver(mRefreshUiReceiver);
    }

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
        mSendServiceMessenger = new Messenger(service);
        mBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName className) {
        mSendServiceMessenger = null;
        mBound = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        dismissProgressDialog();
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
            mProgressShown = true;
        }
    }

    public static class IncomingHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public IncomingHandler(MainActivity activity) {
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity == null) return;
            Log.wtf("activity", "received " + msg.what);
            switch (msg.what) {
                case ReceiveService.LAUNCHED_SERVER:
                    activity.mConnectionState = SendService.STATE_CONNECTED;
                    activity.mServiceButton.setState(SendService.STATE_CONNECTED);
                    activity.dismissProgressDialog();
                    activity.mProgressShown = false;
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

}


