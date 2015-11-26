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

/**
 * Created by Olayinka on 8/15/2015.
 */
public class AbstractAppSettings {
    public static final int PORT = 16916;

    public static final byte MESSAGE_FILE_TRANSFER_EXIT = -128;
    public static final byte MESSAGE_FILE_TRANSFER_ENTRY = 127;
    public static final byte MESSAGE_INIT_CONNECT = 100;

    public static final byte AUTH_FAILED = 105;
    public static final byte AUTH_SUCCESS = 106;
    public static final byte AUTH_ACCESS_DENIED = 107;

    public static final int PROGRESS_FAILED = -4;
    public static final int PROGRESS_MSG = -5;


    public static final int BUFFER_SIZE = 4096;



    public static final String PROGRESS_REQUEST_AUTH = "request.authentication";
    public static final String PROGRESS_CONNECT_SUCCESS = "progress.connect.success";
    public static final String PROGRESS_AUTH_SUCCESS = "progress.auth.success";
    public static final String PROGRESS_FILE_ERROR = "progress.file.error";
    public static final String PROGRESS_INIT = "progress.init";
    public static final String PROGRESS_AUTH_CODE = "progress.auth.code";
    public static final String PROGRESS_DEVICE_REGISTERED = "progress.device.registered";


    public static final int RETURN_TRANSFER_SUCCESS = 0;
    public static final int RETURN_CONNECTION_LOST = 1;
    public static final int RETURN_AUTH_FAILED = 2;
    public static final int RETURN_FAILED_FILE = 3;
    public static final int RETURN_SERVER_EXIT = 4;
    public static final int RETURN_CONNECTION_LOST_CLIENT = 5;
    public static final int RETURN_CONNECTION_LOST_SERVER = 6;
    public static final Integer RETURN_CLIENT_EXIT = 7;

    public static final byte FILE_PROGRESS_START = -1;
    public static final byte FILE_PROGRESS_END = -2;

    public static final String MAIN_LOG = "application.log";
}
