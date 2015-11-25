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

package android.olayinka.file.transfer.adapter;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.olayinka.file.transfer.AppSqlHelper;
import android.olayinka.file.transfer.model.SQLiteDeviceProvider;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.olayinka.file.transfer.R;
import com.olayinka.file.transfer.content.AbstractDevice;
import com.olayinka.file.transfer.model.Device;

import java.sql.Timestamp;

/**
 * Created by Olayinka on 8/14/2015.
 */
public class DeviceAdapter extends CursorAdapter {
    private final AdapterView.OnClickListener mListener;

    public DeviceAdapter(Context context, AppSqlHelper sqlHelper, AdapterView.OnClickListener listener) {
        super(context, sqlHelper.getReadableDatabase().query(Device.TABLE, null, null, null, null, null, Device.Columns._ID + " DESC"), false);
        mListener = listener;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.device_item, null);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.device);
        view.setOnClickListener(mListener);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ContentValues contentValues = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, contentValues);
        Device device = SQLiteDeviceProvider.deviceFromContentValues(contentValues);
        Timestamp timestamp = new Timestamp(device.getLastAccess());
        ImageView imageView = (ImageView) view.findViewById(R.id.icon);
        if (device.getDeviceType().equals(AbstractDevice.DeviceType.DESKTOP.toString()))
            imageView.setImageResource(R.drawable.ic_desktop_mac_black_48dp);
        else
            imageView.setImageResource(R.drawable.ic_phone_android_black_48dp);
        TextView nameView = (TextView) view.findViewById(R.id.name);
        TextView macView = (TextView) view.findViewById(R.id.macAddress);
        TextView ipView = (TextView) view.findViewById(R.id.ipAddress);

        nameView.setText(device.getDisplayName());
        macView.setText(device.getMacAddress().substring(0, 16).toUpperCase());
        ipView.setText(device.getLastKnownIp());
        view.setTag(R.id.device_id, device.getId());

    }

    public void changeCursor(AppSqlHelper sqlHelper) {
        changeCursor(sqlHelper.getReadableDatabase().query(Device.TABLE, null, null, null, null, null, null));
    }
}
