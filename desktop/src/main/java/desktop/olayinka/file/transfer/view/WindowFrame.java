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

import com.olayinka.file.transfer.A2PServerListener;
import desktop.olayinka.file.transfer.AppSettings;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by olayinka on 7/30/15.
 */

public class WindowFrame extends JFrame implements A2PServerListener {


    public WindowFrame(String availableNetworkView) {
        super(availableNetworkView);
        initComponents();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Closed");
            }
        });
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMaximumSize(new java.awt.Dimension(AppSettings.WINDOW_WIDTH * 2, AppSettings.WINDOW_HEIGHT));
        setMinimumSize(new java.awt.Dimension(AppSettings.WINDOW_WIDTH * 2, AppSettings.WINDOW_HEIGHT));
        setPreferredSize(new java.awt.Dimension(AppSettings.WINDOW_WIDTH * 2, AppSettings.WINDOW_HEIGHT));
        setResizable(false);
        //setUndecorated(true);
        pack();
    }

    @Override
    public void exit(String code, String s, Exception e) {

    }

    @Override
    public void message(String code, String s, Exception e) {

    }
}
