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

package android.olayinka.file.transfer;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.olayinka.file.transfer.model.SQLiteDeviceProvider;
import android.olayinka.file.transfer.model.SQLiteTransferProvider;
import com.olayinka.file.transfer.R;
import com.olayinka.file.transfer.model.TransferProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Olayinka on 8/14/2015.
 */
public class AppSqlHelper extends SQLiteOpenHelper {

    public static final String APP_INFO = "app_info";
    private static final String DATABASE_NAME = "smart_tone";
    private static final int DATABASE_VERSION = 9;
    private static AppSqlHelper sInstance;
    private final Context mContext;
    private final SQLiteDeviceProvider mDeviceProvider = new SQLiteDeviceProvider(this);
    private final TransferProvider transferProvider = new SQLiteTransferProvider(this);

    public AppSqlHelper(Context context) {
        super(context, AppSqlHelper.DATABASE_NAME, null, AppSqlHelper.DATABASE_VERSION);
        mContext = context;
    }

    public static AppSqlHelper instance(Context context) {
        if (sInstance == null)
            sInstance = new AppSqlHelper(context.getApplicationContext());
        return sInstance;
    }

    public static boolean hasData(Context context, String table) {
        SQLiteDatabase database = instance(context).getReadableDatabase();
        Cursor cursor = database.query(table, new String[]{"count(*)"}, null, null, null, null, null);
        if (cursor.moveToNext()) {
            return cursor.getInt(0) != 0;
        }
        cursor.close();
        throw new RuntimeException("Must return a row");
    }

    public SQLiteDeviceProvider getDeviceProvider() {
        return mDeviceProvider;
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys = ON;");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String query : makeQueries(R.raw.db))
            db.execSQL(query);

    }

    private List<String> makeQueries(int resourceId) {
        InputStream inputStream = mContext.getResources().openRawResource(resourceId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        List<String> queries = new ArrayList<>();
        String line;
        try {
            String query = "";
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                query += line + " ";
                if (!line.isEmpty() && line.charAt(line.length() - 1) == ';') {
                    queries.add(query);
                    query = "";
                }
            }
        } catch (IOException ignored) {
        }

        return queries;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public TransferProvider getTransferProvider() {
        return transferProvider;
    }

}
