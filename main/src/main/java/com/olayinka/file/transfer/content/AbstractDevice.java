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
 * Created by Olayinka on 9/23/2015.
 */
public abstract class AbstractDevice {

    public static final String TABLE = "device";


    public enum DeviceType {
        MOBILE, DESKTOP
    }

    public static class Status {
        public static final Short ACTIVE = 0;
        public static final Short BANNED = 1;
    }

    public static class Columns extends AbstractAppContent.DeviceColumns {
    }
}
