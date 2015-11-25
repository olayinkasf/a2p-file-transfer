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

import android.content.Intent;
import android.olayinka.file.transfer.AppSqlHelper;
import android.olayinka.file.transfer.Utils;
import android.olayinka.file.transfer.adapter.DeviceAdapter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.olayinka.file.transfer.R;
import com.olayinka.file.transfer.model.Device;
import com.olayinka.file.transfer.model.DeviceProvider;
import ripped.android.json.JSONObject;
import zxing.barcode.scanning.IntentIntegrator;
import zxing.barcode.scanning.IntentResult;

/**
 * Created by Olayinka on 8/14/2015.
 */
public class SelectDeviceActivity extends AppCompatActivity {
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
        initToolbar();
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
            JSONObject object = new JSONObject(scanResult.getContents());
            Device device = Device.deviceFromJSONObject(object);
            Utils.toast(this, String.format(getString(R.string.scanned_device), device.getName(), device.getLastKnownIp()));
            if (!(mDeviceProvider.updateDevice(device) || mDeviceProvider.insertDevice(device))) {
                throw new RuntimeException("There is an unspeakable treachery here!!");
            }

            if (device.getStatus().equals(Device.Status.BANNED)) {
                Utils.toast(this, "This device has been previously deleted!\nPlease undo to proceed");
                return;
            }

            mAdapter.changeCursor(AppSqlHelper.instance(this));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.select_device);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
