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

package com.olayinka.file.transfer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by olayinka on 7/31/15.
 */
public class A2PClient {

    private final A2PServer mServer;
    private ListenerProvider mListenerProvider;
    private String mIpAddress;
    private final int mId;
    DataInputStream mInputStream;
    DataOutputStream mOutputStream;
    Socket mSocket;
    byte mAuthResult = AbstractAppSettings.AUTH_FAILED;
    private int mAuthCounter = 0;

    public static final int MAX_BYTES = 4096;

    public A2PClient(A2PServer server, Socket client) throws IOException {
        mSocket = client;
        mOutputStream = new DataOutputStream(mSocket.getOutputStream());
        mInputStream = new DataInputStream(mSocket.getInputStream());
        mServer = server;
        mIpAddress = client.getInetAddress().getHostAddress();
        mId = (int) (System.nanoTime() % Integer.MAX_VALUE);
    }

    public void setListenerProvider(ListenerProvider mListenerProvider) {
        this.mListenerProvider = mListenerProvider;
    }

    public void run() {
        while (mSocket.isConnected()) {
            try {
                mServer.updateStatus(this, true);
                if (mInputStream.available() == 0) {
                    continue;
                }
                byte message = mInputStream.readByte();
                LOG.w("readData", "Received request type: " + message);
                //TODO handle message type

                if (message == AbstractAppSettings.MESSAGE_FILE_TRANSFER_ENTRY && mAuthResult == AbstractAppSettings.AUTH_SUCCESS) {
                    new FileTransfer(mInputStream, mOutputStream).initiateTransfer(newTransferProgressListener());
                } else if (message == AbstractAppSettings.MESSAGE_FILE_TRANSFER_EXIT && mAuthResult == AbstractAppSettings.AUTH_SUCCESS) {
                    mOutputStream.write(new byte[]{AbstractAppSettings.MESSAGE_FILE_TRANSFER_EXIT});
                    mOutputStream.flush();
                } else if (message == AbstractAppSettings.MESSAGE_INIT_CONNECT && mAuthResult == AbstractAppSettings.AUTH_FAILED && mAuthCounter < 3) {
                    mAuthResult = new DeviceConnect(mInputStream, mOutputStream).connect(newDeviceConnectListener(), mIpAddress);
                    mAuthCounter++;
                } else {
                    mSocket.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
                disconnect(e);
                return;
            }
        }
    }

    private FileTransfer.FileTransferListener newTransferProgressListener() {
        return mListenerProvider.newFileTransferListener();
    }

    private DeviceConnect.DeviceConnectListener newDeviceConnectListener() {
        return mListenerProvider.newDeviceConnectListener();
    }

    public int getId() {
        return mId;
    }

    public Socket getSocket() {
        return mSocket;
    }

    public void disconnect(Exception e) {

        try {
            mInputStream.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            mOutputStream.flush();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            mSocket.shutdownInput();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            mSocket.shutdownOutput();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            mSocket.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        mServer.dispatchMessage(A2PServer.CLIENT_READ_FAIL, "Couldn't read data from " + mIpAddress + ", please reconnect!", e);
        mServer.updateStatus(this, false);
    }

    public interface ListenerProvider {

        FileTransfer.FileTransferListener newFileTransferListener();

        DeviceConnect.DeviceConnectListener newDeviceConnectListener();

    }

    public String getIpAddress() {
        return mIpAddress;
    }
}
