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

import desktop.olayinka.file.transfer.AppContext;
import desktop.olayinka.file.transfer.AppSettings;
import com.olayinka.file.transfer.LOG;
import desktop.olayinka.file.transfer.adapter.AvailableNetworkRenderer;
import desktop.olayinka.file.transfer.model.AvailableNetwork;

import javax.swing.*;
import java.net.SocketException;

/**
 * Created by olayinka on 7/30/15.
 */
public class AvailableNetworkList extends JList<Object> {

    AppContext mContext;

    public AvailableNetworkList(AppContext context) {
        mContext = context;
        AvailableNetwork avInetAddresses = null;
        setPreferredSize(AppSettings.NETWORK_DIMENS);
        setMaximumSize(AppSettings.NETWORK_DIMENS);
        setMinimumSize(AppSettings.NETWORK_DIMENS);
        setSelectionBackground(AppSettings.APP_COLOR);
        try {
            avInetAddresses = AvailableNetwork.getAvailableNetwork();
            for (int i = 0; i < avInetAddresses.getSize(); i++)
                LOG.w("listModel", String.valueOf(avInetAddresses.get(i)));
        } catch (SocketException e) {
            JOptionPane.showMessageDialog(getRootPane(),
                    "Eggs are not supposed to be green.",
                    "Inane error",
                    JOptionPane.ERROR_MESSAGE);
        }
        ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
        toolTipManager.registerComponent(this);
        setLayoutOrientation(VERTICAL);
        assert avInetAddresses != null;
        setModel(avInetAddresses);
        setCellRenderer(new AvailableNetworkRenderer());
        setAutoscrolls(true);
        updateUI();
    }

    public void rescan() {
        try {
            AvailableNetwork avInetAddresses = AvailableNetwork.getAvailableNetwork();
            for (int i = 0; i < avInetAddresses.getSize(); i++)
                LOG.w("listModel", String.valueOf(avInetAddresses.get(i)));
            setModel(avInetAddresses);
        } catch (SocketException e) {
            JOptionPane.showMessageDialog(getRootPane(),
                    "Eggs are not supposed to be green.",
                    "Inane error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
