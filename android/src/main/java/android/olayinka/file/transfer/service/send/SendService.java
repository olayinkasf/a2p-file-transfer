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

package android.olayinka.file.transfer.service.send;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.olayinka.file.transfer.model.SQLiteDeviceProvider;
import android.os.Parcelable;

import java.util.Arrays;

/**
 * Created by Olayinka on 8/14/2015.
 */
public class SendService extends IntentService {
    public static final String DEVICE = "device";
    public static final String DATA = "data";
    public static final String NAME = "Send Service";
    public static final String STATE_CONNECTED = "state.connected";

    public SendService() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ContentValues device = intent.getParcelableExtra(DEVICE);
        Parcelable[] parcelables = intent.getParcelableArrayExtra(DATA);
        Uri[] uris = Arrays.copyOf(parcelables, parcelables.length, Uri[].class);
        A2PClient client = new A2PClient(this, SQLiteDeviceProvider.deviceFromContentValues(device));
        client.executeNow(uris);
    }

}
