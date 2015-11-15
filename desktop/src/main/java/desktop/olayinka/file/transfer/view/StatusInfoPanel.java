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

import com.olayinka.file.transfer.A2PServer;
import com.olayinka.file.transfer.A2PServerListener;
import desktop.olayinka.file.transfer.AppContext;
import desktop.olayinka.file.transfer.AppSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.util.Objects;

/**
 * Created by olayinka on 7/31/15.
 */
public class StatusInfoPanel extends JPanel implements A2PServerListener {

    private JButton mStatusButton;
    private AppContext mContext;
    private QRCodePanel mQRCodePanel;

    public StatusInfoPanel(AppContext context) {
        mContext = context;
        createUIComponents();
        mStatusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mContext.isActive()) {
                    disconnectDialog();
                } else {
                    connectDialog();
                }
            }
        });
    }

    private void connectDialog() {
        int option = JOptionPane.showConfirmDialog(getRootPane(),
                "Open a connection on this machine?",
                "Connect",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            mContext.connect(this);
        }

    }

    private void disconnectDialog() {
        int option = JOptionPane.showConfirmDialog(getRootPane(),
                "Close connection on this machine? All connected devices and current transfers will be stopped.",
                "Disconnect",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            mContext.disconnect();
        }

    }

    public void revalidate(final InetAddress inetAddress) {
        if (inetAddress == null) {
            setVisible(false);
            return;
        }
        mQRCodePanel.revalidate(inetAddress);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        mStatusButton = new JButton();
        mStatusButton.setBackground(AppSettings.APP_COLOR);
        //noinspection StringEquality
        if (AppSettings.LOOK_AND_FEEL == AppSettings.LF_WINDOWS)
            mStatusButton.setForeground(AppSettings.APP_COLOR);
        else mStatusButton.setForeground(Color.WHITE);

        setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        setPreferredSize(AppSettings.STATUS_INFO_DIMENS);
        setMaximumSize(AppSettings.STATUS_INFO_DIMENS);
        setMinimumSize(AppSettings.STATUS_INFO_DIMENS);
        setBackground(AppSettings.APP_COLOR);
        //setOpaque(false);

        mQRCodePanel = new QRCodePanel(mContext);
        add(mQRCodePanel);

        JPanel buttonWrapperPanel = new JPanel();
        buttonWrapperPanel.setLayout(new GridBagLayout());
        buttonWrapperPanel.setBackground(AppSettings.APP_COLOR);
        int infoWidth = AppSettings.WINDOW_WIDTH - AppSettings.STATUS_BAR_HEIGHT;
        buttonWrapperPanel.setPreferredSize(new Dimension(infoWidth, AppSettings.STATUS_BAR_HEIGHT));
        add(buttonWrapperPanel);

        mStatusButton.setPreferredSize(new Dimension(110, 40));
        mStatusButton.setText("CONNECT");
        buttonWrapperPanel.add(mStatusButton);

        setVisible(false);
    }

    @Override
    public void setVisible(boolean aFlag) {
        mQRCodePanel.setVisible(aFlag);
    }

    @Override
    public void exit(String code, String s, Exception e) {
        mStatusButton.setText("CONNECT");
    }

    @Override
    public void message(String code, String s, Exception e) {
        if (Objects.equals(code, A2PServer.SERVER_INIT_SUCCESS)) {
            mStatusButton.setText("DISCONNECT");
        }
    }

}
