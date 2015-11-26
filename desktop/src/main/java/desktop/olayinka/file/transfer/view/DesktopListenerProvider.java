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

import com.olayinka.file.transfer.A2PClient;
import com.olayinka.file.transfer.AbstractDeviceConnectListener;
import com.olayinka.file.transfer.model.Device;
import com.olayinka.file.transfer.model.DeviceConnection;
import com.olayinka.file.transfer.model.DeviceProvider;
import desktop.olayinka.file.transfer.AppContext;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Olayinka on 8/5/2015.
 */
public class DesktopListenerProvider extends JPanel implements A2PClient.ListenerProvider {
    private final AppContext mContext;
    private final AbstractDeviceConnectListener mDeviceConnectListener = new AbstractDeviceConnectListener() {
        @Override
        protected DeviceProvider getDeviceProvider() {
            return mContext.getJDBCHelper().getDeviceProvider();
        }

        @Override
        public void showAuthCode(final DeviceConnection connection) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    Frame frame = (JFrame) SwingUtilities.getWindowAncestor(DesktopListenerProvider.this);
                    JDialog dialog = new AuthCodeView(frame, connection, true);
                    dialog.pack();
                    dialog.setLocationRelativeTo(frame);
                    dialog.setVisible(true);
                }
            });
        }
    };

    public DesktopListenerProvider(AppContext mContext) {
        this.mContext = mContext;
    }

    @Override
    public A2PClient.FileTransferListener newFileTransferListener(Device device) {
        final DesktopFileTransferListener transferProgressView = new DesktopFileTransferListener(mContext, device);
        JComponent component = transferProgressView.$$$getRootComponent$$$();
        add(component, 0);
        return transferProgressView;
    }

    @Override
    public A2PClient.DeviceConnectListener deviceConnectListener() {
        return mDeviceConnectListener;
    }


}
