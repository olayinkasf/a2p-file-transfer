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
import com.olayinka.file.transfer.A2PServerListener;
import desktop.olayinka.file.transfer.AppContext;
import desktop.olayinka.file.transfer.AppSettings;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * �
 * Created by olayinka on 7/30/15.
 */
public class AvailableNetworkView extends JPanel implements A2PServerListener {
    private AvailableNetworkList mAvailableNetworkList;
    private StatusInfoPanel mStatusInfoPanel;
    private JList mFileList;
    private JPanel mProgressList;
    private JScrollPane mProgressScrollPanel;
    private JToolBar mToolBar;
    private AppContext mContext;
    private JButton mRescanButton = new JButton("   RESCAN   ");
    private JButton mDevicesButton = new JButton("   DEVICES   ");

    public AvailableNetworkView(WindowFrame frame, AppContext mContext) {
        this.mContext = mContext;
        mContext.setRootListener(this);
        createUIComponents();

        mContext.setListenerProvider((A2PClient.ListenerProvider) mProgressList);
        mAvailableNetworkList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;
                Object value = mAvailableNetworkList.getSelectedValue();
                if (value == null) return;
                if (!(value instanceof NetworkInterface)) {
                    InetAddress inetAddress = (InetAddress) value;

                    ///update status bar
                    mStatusInfoPanel.setVisible(true);
                    mStatusInfoPanel.revalidate(inetAddress);
                } else {
                    mStatusInfoPanel.setVisible(false);
                }
            }
        });
    }

    public static void main(String[] args) {
        try {
            boolean lf = false;

            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    System.out.println("Windows Look and feel found!");
                    lf = true;
                    AppSettings.LOOK_AND_FEEL = AppSettings.LF_WINDOWS;
                    break;
                }
            }
            if (!lf) {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        System.out.println("Nimbus Look and feel found!");
                        lf = true;
                        AppSettings.LOOK_AND_FEEL = AppSettings.LF_NIMBUS;
                        break;
                    }
                }
            }
            if (!lf) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                AppSettings.LOOK_AND_FEEL = AppSettings.LF_DEFAULT;
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(AvailableNetworkView.class.getName()).log(Level.SEVERE, null, ex);
        }
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                initView();
            }
        });

    }

    private static void initView() {
        WindowFrame frame = new WindowFrame("A2P File Transfer");
        AppContext appContext = AppContext.initialize(frame);
        AvailableNetworkView frameWrapper = new AvailableNetworkView(frame, appContext);
        Insets insets = frame.getInsets();
        System.out.println(insets);
        int frameWidth = AppSettings.WINDOW_WIDTH * 2 + insets.left + insets.right;
        int frameHeight = AppSettings.WINDOW_HEIGHT + insets.top + insets.bottom;
        Dimension dimension = new Dimension(frameWidth, frameHeight);
        frame.setMinimumSize(dimension);
        frame.setMaximumSize(dimension);
        frame.setPreferredSize(dimension);
        frame.setContentPane(frameWrapper);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

    }

    private void createUIComponents() {

        setBackground(AppSettings.APP_COLOR);
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setPreferredSize(AppSettings.WINDOW_DIMENS);
        setMaximumSize(AppSettings.WINDOW_DIMENS);
        setMinimumSize(AppSettings.WINDOW_DIMENS);

        mToolBar = new JToolBar();
        mToolBar.setPreferredSize(AppSettings.TOOL_BAR_DIMENS);
        mToolBar.setMaximumSize(AppSettings.TOOL_BAR_DIMENS);
        mToolBar.setMinimumSize(AppSettings.TOOL_BAR_DIMENS);
        mToolBar.setFloatable(false);
        add(mToolBar);

        mToolBar.add(mRescanButton);
        mToolBar.add(mDevicesButton);

        mRescanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mAvailableNetworkList.rescan();
            }
        });
        mDevicesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Frame frame = (JFrame) SwingUtilities.getWindowAncestor(AvailableNetworkView.this);
                JDialog dialog = new DeviceList(mContext, frame, true);
                dialog.pack();
                dialog.setLocationRelativeTo(frame);
                dialog.setVisible(true);
            }
        });

        mAvailableNetworkList = new AvailableNetworkList(mContext);
        add(mAvailableNetworkList);
        mFileList = new JList();
        mFileList.setPreferredSize(AppSettings.NETWORK_DIMENS);
        mFileList.setMaximumSize(AppSettings.NETWORK_DIMENS);
        mFileList.setMinimumSize(AppSettings.NETWORK_DIMENS);
        mFileList.setSelectionBackground(AppSettings.APP_COLOR);
        add(mFileList);


        mStatusInfoPanel = new StatusInfoPanel(mContext);
        add(mStatusInfoPanel);

        mProgressList = new DesktopListenerProvider(mContext);
        mProgressList.setLayout(new WrapLayout(FlowLayout.CENTER, 3, 3));
        mProgressList.setBackground(AppSettings.APP_COLOR);

        mProgressScrollPanel = new JScrollPane();
        mProgressScrollPanel.setMinimumSize(AppSettings.STATUS_INFO_DIMENS);
        mProgressScrollPanel.setMaximumSize(AppSettings.STATUS_INFO_DIMENS);
        mProgressScrollPanel.setPreferredSize(AppSettings.STATUS_INFO_DIMENS);
        mProgressScrollPanel.setBackground(AppSettings.APP_COLOR);
        mProgressScrollPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mProgressScrollPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        mProgressScrollPanel.setBorder(BorderFactory.createEmptyBorder());
        mProgressScrollPanel.setViewportView(mProgressList);
        add(mProgressScrollPanel);


        //TODO DELETE
        /*for (int i = 1; i < 100; i++) {
            final DesktopFileTransferListener label2 = new DesktopFileTransferListener();
            JComponent component = label2.$$$getRootComponent$$$();
            mProgressList.add(component);
            mProgressList.revalidate();
        }*/
    }

    @Override
    public void exit(String code, String s, Exception e) {

    }

    @Override
    public void message(String code, String s, Exception e) {

    }


}
