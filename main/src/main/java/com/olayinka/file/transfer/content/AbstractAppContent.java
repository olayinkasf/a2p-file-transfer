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

package com.olayinka.file.transfer.content;

/**
 * Created by Olayinka on 9/24/2015.
 */
public class AbstractAppContent {

    public static final String _ID = "_id";
    public static final String EQUALS = " = ? ";
    public static final String APP_INFO = "app_info";

    public static class TransferColumns {
        public static final String _ID = AbstractAppContent._ID;
        public static final String DEVICE_ID = "device" + _ID;
        public static final String TRANSFER_TYPE = "transfer_type";
        public static final String FILE_NAME = "file_name";
        public static final String EXPECTED_SIZE = "expected_size";
        public static final String TRANSFERRED_SIZE = "transferred_size";
        public static final String TIME = "time";
        public static final String STATUS = "status";
    }

    public static class DeviceColumns {
        public static final String _ID = AbstractAppContent._ID;
        public static final String NAME = "name";
        public static final String DISPLAY_NAME = "display_name";
        public static final String MAC_ADDRESS = "mac_address";
        public static final String DEVICE_TYPE = "device_type";
        public static final String AUTH_HASH = "auth_hash";
        public static final String LAST_KNOWN_IP = "last_known_ip";
        public static final String STATUS = "status";
        public static final String LAST_ACCESS = "last_access";
    }

}
