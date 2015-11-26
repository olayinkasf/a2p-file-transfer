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
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.olayinka.file.transfer.AppSqlHelper;
import android.olayinka.file.transfer.Utils;
import android.olayinka.file.transfer.model.SQLiteTransferProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.olayinka.file.transfer.R;
import com.olayinka.file.transfer.model.Transfer;

import java.io.File;
import java.sql.Timestamp;

/**
 * Created by Olayinka on 8/14/2015.
 */
public class TransferAdapter extends CursorAdapter {
    public TransferAdapter(Context context) {
        super(context, AppSqlHelper.instance(context).getReadableDatabase().query("transfer", null, null, null, null, null, Transfer.Columns._ID + " DESC"), false);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.transfer_item, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MimeTypeMap myMime = MimeTypeMap.getSingleton();
                    Intent newIntent = new Intent(Intent.ACTION_VIEW);
                    String mimeType = myMime.getMimeTypeFromExtension(fileExt(((File) v.getTag(R.id.contentUri)).getAbsolutePath()).substring(1));
                    newIntent.setDataAndType(Uri.fromFile((File) v.getTag(R.id.contentUri)), mimeType);
                    newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    v.getContext().startActivity(newIntent);
                } catch (Exception e) {
                    Utils.toast(v.getContext(), R.string.no_activity_found);
                }
            }
        });
        return view;
    }

    private String fileExt(String url) {
        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf("."));
            if (ext.contains("%")) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.contains("/")) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ContentValues contentValues = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, contentValues);
        Transfer transfer = SQLiteTransferProvider.transferFromContentValues(contentValues);
        Timestamp timestamp = new Timestamp(transfer.getTime());
        ImageView imageView = (ImageView) view.findViewById(R.id.icon);
        if (transfer.getTransferType().equals(Transfer.TransferType.RECEIVED.toString()))
            imageView.setImageResource(R.drawable.ic_subdirectory_arrow_right_black_36dp);
        else
            imageView.setImageResource(R.drawable.ic_subdirectory_arrow_left_black_36dp);
        TextView nameView = (TextView) view.findViewById(R.id.name);
        TextView deviceName = (TextView) view.findViewById(R.id.deviceName);
        TextView timeView = (TextView) view.findViewById(R.id.time);

        nameView.setText(transfer.getFile().getName());
        deviceName.setText("Unknown Device");
        timeView.setText(timestamp.toString());
        view.setTag(R.id.contentUri, transfer.getFile());
    }
}
