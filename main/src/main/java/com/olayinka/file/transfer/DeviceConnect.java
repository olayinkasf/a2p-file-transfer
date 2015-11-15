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

package com.olayinka.file.transfer;

import com.olayinka.file.transfer.model.Device;
import ripped.android.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Olayinka on 9/24/2015.
 */
public class DeviceConnect {

    public DeviceConnect(DataInputStream mInputStream, DataOutputStream mOutputStream) {
        this.mInputStream = mInputStream;
        this.mOutputStream = mOutputStream;
    }

    DataInputStream mInputStream;
    DataOutputStream mOutputStream;

    public byte connect(DeviceConnectListener fileTransferListener, String ipAddress) throws IOException {
        JSONObject object = new JSONObject(SocketUtils.readStringData(mInputStream));
        object.put(Device.Columns.LAST_KNOWN_IP, ipAddress);
        fileTransferListener.registerDevice(object);
        byte result = (byte) object.getInt(AbstractAppSettings.AUTH_RESULT);
        mOutputStream.write(result);
        mOutputStream.flush();
        if (result == AbstractAppSettings.AUTH_FAILED) {
            /**
             * Authentication should be done by the server, always
             */
/*            try {
                SocketUtils.writeStringData(mOutputStream, hash(object.getString(Device.Columns.AUTH_HASH)));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                System.exit(1);
                throw new RuntimeException();
            }*/
            fileTransferListener.showAuthCode(object);
        }
        return result;
    }

    public interface DeviceConnectListener {

        /**
         * register device, persist or update if possible, modify JSONObject to send result back to invoker
         *
         * @param object
         */
        Device registerDevice(JSONObject object);

        /**
         * Show authentication code on the device screen, this method should be invoked on the UI thread
         *
         * @param object
         */
        void showAuthCode(JSONObject object);
    }
}
