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

package desktop.olayinka.file.transfer.adapter;

import desktop.olayinka.file.transfer.AppSettings;
import desktop.olayinka.file.transfer.model.AvailableNetwork;
import desktop.olayinka.file.transfer.view.NetworkInterfaceView;

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

import static com.olayinka.file.transfer.Utils.macToString;


/**
 * Created by olayinka on 7/30/15.
 */

public class AvailableNetworkRenderer extends JLabel implements ListCellRenderer<Object> {


    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

        if (value instanceof NetworkInterface) {
            NetworkInterface networkInterface = (NetworkInterface) value;
            NetworkInterfaceView view = new NetworkInterfaceView();
            view.getWrapperPanel().setBackground(AppSettings.LIST_HEADER_COLOR);
            view.getDisplayNameLabel().setText(networkInterface.getDisplayName());
            try {
                String macAddress = macToString(networkInterface.getHardwareAddress());
                view.getMACAddressLabel().setText(macAddress);
            } catch (SocketException e) {
                view.getMACAddressLabel().setText(AvailableNetwork.MAC_ERROR);
            }
            view.getWrapperPanel().setEnabled(false);
            return view.getWrapperPanel();
        } else {
            setFont(new Font(getFont().getName(), Font.BOLD, getFont().getSize()));
            setMaximumSize(new Dimension(460, 40));
            setMinimumSize(new Dimension(460, 40));
            setPreferredSize(new Dimension(460, 40));
            if (isSelected) {
                setForeground(Color.LIGHT_GRAY);
                setBackground(AppSettings.APP_COLOR);
                setOpaque(true);
            } else {
                setForeground(list.getForeground());
                setBackground(list.getBackground());
            }
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            InetAddress inetAddress = (InetAddress) value;
            setText(inetAddress.getHostAddress());
            return this;
        }

    }
}

