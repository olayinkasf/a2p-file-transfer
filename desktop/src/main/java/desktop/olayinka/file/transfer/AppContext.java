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


import com.olayinka.file.transfer.A2PClient;
import com.olayinka.file.transfer.A2PServerListener;
import com.olayinka.file.transfer.model.AppInfo;
import desktop.olayinka.file.transfer.model.DerbyJDBCHelper;
import desktop.olayinka.file.transfer.view.WindowFrame;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by olayinka on 7/31/15.
 */
public class AppContext extends WindowAdapter implements A2PServerListener {
    static AppContext APP_CONTEXT;
    final WindowFrame mRootFrame;
    private A2PServerListener mRootListener;
    private A2PServerThread mServer;
    private A2PClient.ListenerProvider mListenerProvider;
    private DerbyJDBCHelper mJDBCHelper = DerbyJDBCHelper.instance();
    AppInfo mSystemProperties;

    public void setRootListener(A2PServerListener mRootListener) {
        this.mRootListener = mRootListener;
    }

    private AppContext(WindowFrame mRootFrame) {
        this.mRootFrame = mRootFrame;
        mRootFrame.addWindowListener(this);
        mSystemProperties = mJDBCHelper.getSystemProperties();
    }

    public AppInfo getSystemProperties() {
        return mSystemProperties;
    }

    public static AppContext initialize(WindowFrame frame) {
        if (APP_CONTEXT != null)
            throw new RuntimeException("Context already created!");
        APP_CONTEXT = new AppContext(frame);
        return APP_CONTEXT;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        try {
            disconnect();
            mJDBCHelper.shutdown();
            super.windowClosing(e);
        } catch (Throwable ignored) {
        }
        System.exit(0);
    }

    public boolean isActive() {
        return mServer != null && mServer.isActive();
    }

    public void connect(A2PServerListener listener) {
        disconnect();
        mServer = new A2PServerThread(listener, mRootListener, this);
        mServer.setListenerProvider(mListenerProvider);
        new Thread(mServer).start();
    }

    public void disconnect() {
        if (mServer != null) {
            mServer.disconnect();
        }
        mServer = null;
    }

    public WindowFrame getRootFrame() {
        return mRootFrame;
    }

    public void setListenerProvider(A2PClient.ListenerProvider mListenerProvider) {
        this.mListenerProvider = mListenerProvider;
    }

    @Override
    public void exit(String code, String s, Exception e) {
        mServer = null;
    }

    @Override
    public void message(String code, String s, Exception e) {

    }

    public DerbyJDBCHelper getJDBCHelper() {
        return mJDBCHelper;
    }
}
