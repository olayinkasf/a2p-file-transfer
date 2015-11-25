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

import javax.swing.*;
import java.awt.*;

/**
 * Created by olayinka on 7/30/15.
 */
public class NetworkInterfaceView {
    private JLabel mDisplayNameLabel;
    private JLabel mMACAddressLabel;
    private JPanel mWrapperPanel;

    public JLabel getDisplayNameLabel() {
        return mDisplayNameLabel;
    }

    public JLabel getMACAddressLabel() {
        return mMACAddressLabel;
    }

    public JPanel getWrapperPanel() {
        return mWrapperPanel;
    }

    {
        $$$setupUI$$$();
    }

    private void $$$setupUI$$$() {
        mWrapperPanel = new JPanel();
        mWrapperPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        mWrapperPanel.setMaximumSize(new Dimension(480, 50));
        mWrapperPanel.setMinimumSize(new Dimension(480, 50));
        mWrapperPanel.setPreferredSize(new Dimension(480, 50));
        mWrapperPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), null));
        mDisplayNameLabel = new JLabel();
        mDisplayNameLabel.setFont(new Font(mDisplayNameLabel.getFont().getName(), Font.BOLD, mDisplayNameLabel.getFont().getSize()));
        mDisplayNameLabel.setForeground(new Color(-1));
        mDisplayNameLabel.setHorizontalAlignment(2);
        mDisplayNameLabel.setHorizontalTextPosition(2);
        mDisplayNameLabel.setPreferredSize(new Dimension(460, 20));
        mDisplayNameLabel.setText("Label");
        mWrapperPanel.add(mDisplayNameLabel);
        mMACAddressLabel = new JLabel();
        mMACAddressLabel.setFont(new Font(mMACAddressLabel.getFont().getName(), Font.ITALIC, mMACAddressLabel.getFont().getSize()));
        mMACAddressLabel.setForeground(new Color(-1));
        mMACAddressLabel.setHorizontalAlignment(2);
        mMACAddressLabel.setHorizontalTextPosition(2);
        mMACAddressLabel.setPreferredSize(new Dimension(460, 20));
        mMACAddressLabel.setText("Label");
        mWrapperPanel.add(mMACAddressLabel);
    }

    public JComponent $$$getRootComponent$$$() {
        return mWrapperPanel;
    }
}
