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
import android.content.ContentValues;
import android.content.Intent;
import android.olayinka.file.transfer.AppSqlHelper;
import android.olayinka.file.transfer.Utils;
import android.olayinka.file.transfer.adapter.DeviceAdapter;
import android.olayinka.file.transfer.model.SQLiteDeviceProvider;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.olayinka.file.transfer.AbstractAppSettings;
import com.olayinka.file.transfer.R;
import com.olayinka.file.transfer.model.Device;
import com.olayinka.file.transfer.model.DeviceProvider;
import zxing.barcode.scanning.IntentIntegrator;
import zxing.barcode.scanning.IntentResult;

/**
 * Created by Olayinka on 8/14/2015.
 */
public class SelectDeviceActivity extends Activity {
    public static final String SELECTED_DEVICE_ID = "selected.device.id";
    View.OnClickListener mSendButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            IntentIntegrator integrator = new IntentIntegrator(SelectDeviceActivity.this);
            integrator.initiateScan();
        }
    };
    ListView mDeviceListView;
    AdapterView.OnClickListener mDeviceSelectListener = new AdapterView.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.putExtra(SELECTED_DEVICE_ID, (Long) v.getTag(R.id.device_id));
            setResult(RESULT_OK, intent);
            finish();
        }

    };
    private DeviceAdapter mAdapter;
    private DeviceProvider mDeviceProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device);
        findViewById(R.id.sendFile).setOnClickListener(mSendButtonClickListener);
        mDeviceListView = (ListView) findViewById(R.id.list);
        mAdapter = new DeviceAdapter(this, AppSqlHelper.instance(this), mDeviceSelectListener);
        mDeviceListView.setAdapter(mAdapter);
        mDeviceProvider = AppSqlHelper.instance(this).getDeviceProvider();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null && scanResult.getContents() != null) {
            String[] result = scanResult.getContents().split(AbstractAppSettings.SPLIT_TOKEN, 4);
            Utils.toast(this, getString(R.string.mac_address) + ": " + result[0] + "\n"
                    + getString(R.string.ip_address) + ": " + result[1]);
            ContentValues values = new ContentValues();
            values.put(Device.Columns.DEVICE_ID_HASH, result[0]);
            values.put(Device.Columns.LAST_KNOWN_IP, result[1]);
            values.put(Device.Columns.NAME, result[2]);
            values.put(Device.Columns.DEVICE_TYPE, result[3]);
            Device device = SQLiteDeviceProvider.deviceFromContentValues(values);

            if(!(mDeviceProvider.updateDevice(device)|| mDeviceProvider.insertDevice(device))){
                throw new RuntimeException("There is an unspeakable treachery here!!");
            }

            if(device.getStatus().equals(Device.Status.BANNED)){
                Utils.toast(this,"This device has been previously deleted!\nPlease undo to proceed");
                return;
            }

            mAdapter.changeCursor(AppSqlHelper.instance(this));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
