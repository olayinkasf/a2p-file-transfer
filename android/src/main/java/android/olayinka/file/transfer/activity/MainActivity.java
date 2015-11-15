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
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.olayinka.file.transfer.AppSqlHelper;
import android.olayinka.file.transfer.Utils;
import android.olayinka.file.transfer.adapter.TransferAdapter;
import android.olayinka.file.transfer.model.SQLiteDeviceProvider;
import android.olayinka.file.transfer.service.send.SendService;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.olayinka.file.transfer.R;
import com.olayinka.file.transfer.model.Device;
import com.olayinka.file.transfer.model.DeviceProvider;

import java.util.ArrayList;

/**
 * Created by Olayinka on 8/8/2015.
 */
public class MainActivity extends Activity {
    public static final int DEVICE_REQUEST_CODE = 0x4856;
    public static final int PICK_FILE_REQUEST_CODE = 0x4152;
    View.OnClickListener mSendButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            selectFile();
        }
    };
    ListView mLogListView;
    private DeviceProvider mDeviceProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        findViewById(R.id.sendFile).setOnClickListener(mSendButtonClickListener);
        mLogListView = (ListView) findViewById(R.id.list);
        mLogListView.setAdapter(new TransferAdapter(this, AppSqlHelper.instance(this)));
        mDeviceProvider = AppSqlHelper.instance(this).getDeviceProvider();
    }

    Uri[] mWaitingUris;

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
            Device device = mDeviceProvider.findDeviceById(intent.getLongExtra(SelectDeviceActivity.SELECTED_DEVICE_ID, 0l));
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
}


