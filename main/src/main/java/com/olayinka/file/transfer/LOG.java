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
 * Created by olayinka on 7/31/15.
 */
public class LOG {

    public static void w(String context, String arg) {
        System.out.println(context + ":\t" + arg);
    }

    public static void e(String context, String arg) {
        System.err.println(context + ":\t" + arg);
    }

    public static void e(String context, String s, Exception e) {
        System.err.println(context + ":\t" + s + "\n" + e.getMessage());
    }
}
