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

package desktop.olayinka.file.transfer.view;

/**
 * Created by olayinka on 7/30/15.
 */

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.olayinka.file.transfer.Utils;
import desktop.olayinka.file.transfer.AppContext;
import desktop.olayinka.file.transfer.AppSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

public class QRCodePanel extends JPanel {

    private BufferedImage image;
    private final AppContext mContext;

    public QRCodePanel(AppContext mContext) {
        super();
        this.mContext = mContext;
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        setPreferredSize(AppSettings.QR_PANEL_DIMENS);
        setMinimumSize(AppSettings.QR_PANEL_DIMENS);
        setMaximumSize(AppSettings.QR_PANEL_DIMENS);
        setBackground(AppSettings.APP_COLOR);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 10, 10, AppSettings.STATUS_BAR_HEIGHT - 20, AppSettings.STATUS_BAR_HEIGHT - 20, null); // see javadoc for more info on the parameters
    }

    public void revalidate(InetAddress inetAddress) {
        BitMatrix result;
        String macAddress;
        try {
            NetworkInterface network = NetworkInterface.getByInetAddress(inetAddress);
            macAddress = Utils.macToString(network.getHardwareAddress());
        } catch (SocketException e) {
            e.printStackTrace();
            macAddress = "";
        }
        try {
            String toEncode = macAddress + AppSettings.SPLIT_TOKEN
                    + inetAddress.getHostAddress() + AppSettings.SPLIT_TOKEN
                    + mContext.getSystemProperties().getName() + AppSettings.SPLIT_TOKEN
                    + AppSettings.DEVICE_TYPE;

            result = new MultiFormatWriter().encode(toEncode,
                    BarcodeFormat.QR_CODE, AppSettings.STATUS_BAR_HEIGHT - 20, AppSettings.STATUS_BAR_HEIGHT - 20, null);
            image = MatrixToImageWriter.toBufferedImage(result);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
        } catch (WriterException e) {
            e.printStackTrace();
        }
        repaint();
    }
}