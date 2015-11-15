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

package desktop.olayinka.file.transfer;

import com.olayinka.file.transfer.AbstractAppSettings;
import com.olayinka.file.transfer.content.AbstractDevice;

import java.awt.*;

/**
 * Created by olayinka on 7/30/15.
 */
public class AppSettings extends AbstractAppSettings {

    public static AbstractDevice.DeviceType DEVICE_TYPE = AbstractDevice.DeviceType.DESKTOP;

    public static final int WINDOW_WIDTH = 480;
    public static final int WINDOW_HEIGHT = 600;
    public static final int STATUS_BAR_HEIGHT = 200;
    public static final int CODE_DIALOG_SIZE = 300;
    public static final int TOOL_BAR_HEIGHT = 30;
    public static final String LF_NIMBUS = "Nimbus";
    public static final String LF_WINDOWS = "Windows";
    public static final String LF_DEFAULT = "Default";
    public static String LOOK_AND_FEEL;

    public static Dimension WINDOW_DIMENS = new Dimension(WINDOW_WIDTH * 2, WINDOW_HEIGHT);
    public static Dimension TOOL_BAR_DIMENS = new Dimension(WINDOW_WIDTH * 2, TOOL_BAR_HEIGHT);
    public static Dimension NETWORK_DIMENS = new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT - STATUS_BAR_HEIGHT - TOOL_BAR_HEIGHT);
    @SuppressWarnings("SuspiciousNameCombination")
    public static Dimension QR_PANEL_DIMENS = new Dimension(STATUS_BAR_HEIGHT, STATUS_BAR_HEIGHT);
    public static Dimension CODE_DIALOG_DIMENS = new Dimension(CODE_DIALOG_SIZE, CODE_DIALOG_SIZE);

    public static Dimension STATUS_INFO_DIMENS = new Dimension(WINDOW_WIDTH, STATUS_BAR_HEIGHT);

    public static final Color APP_COLOR = new Color(0x00, 0x60, 0x58);
    public static final Color TRANSPARENT = new Color(0x00, 0x00, 0x00, 0x00);
    public static final Color LIST_HEADER_COLOR = new Color(0x00, 0x60, 0x58, 0x80);
    public static final int PORT = 16916;
}
