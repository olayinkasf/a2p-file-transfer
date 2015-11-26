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

import com.olayinka.file.transfer.model.Device;
import com.olayinka.file.transfer.model.DeviceConnection;
import ripped.android.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

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
    private Device mDevice;

    public A2PClient(A2PServer server, Socket client) throws IOException {
        mSocket = client;
        mOutputStream = new DataOutputStream(mSocket.getOutputStream());
        mInputStream = new DataInputStream(mSocket.getInputStream());
        mServer = server;
        mIpAddress = client.getInetAddress().getHostAddress();
        mId = (int) (System.nanoTime() % Integer.MAX_VALUE);
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
                    initiateTransfer(mListenerProvider.newFileTransferListener(mDevice));
                } else if (message == AbstractAppSettings.MESSAGE_FILE_TRANSFER_EXIT && mAuthResult == AbstractAppSettings.AUTH_SUCCESS) {
                    mOutputStream.write(new byte[]{AbstractAppSettings.MESSAGE_FILE_TRANSFER_EXIT});
                    mOutputStream.flush();
                } else if (message == AbstractAppSettings.MESSAGE_INIT_CONNECT && mAuthResult == AbstractAppSettings.AUTH_FAILED && mAuthCounter < 3) {
                    mAuthResult = connect(mIpAddress);
                    mAuthCounter++;
                } else {
                    //mServer.updateStatus(this, false);
                    mSocket.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
                disconnect(e);
                return;
            }
        }
    }

    public void setListenerProvider(ListenerProvider mListenerProvider) {
        this.mListenerProvider = mListenerProvider;
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

        FileTransferListener newFileTransferListener(Device device);

        DeviceConnectListener deviceConnectListener();

    }

    public String getIpAddress() {
        return mIpAddress;
    }

    public Device getConnectedDevice() {
        if (mAuthResult == AbstractAppSettings.AUTH_SUCCESS)
            return mDevice;
        else return null;
    }

    public byte connect(String ipAddress) throws IOException {
        JSONObject object = new JSONObject(SocketUtils.readStringData(mInputStream));
        object.put(Device.Columns.LAST_KNOWN_IP, ipAddress);
        DeviceConnection connection = mListenerProvider.deviceConnectListener().registerDevice(object);
        byte result = connection.getResult();
        mDevice = connection.getDevice();
        mOutputStream.write(result);
        mOutputStream.flush();
        if (result == AbstractAppSettings.AUTH_FAILED) {
            mListenerProvider.deviceConnectListener().showAuthCode(connection);
        }
        return result;
    }

    public void initiateTransfer(FileTransferListener listener) throws IOException, DirectoryAccessException {
        try {
            final byte[] buffer = new byte[A2PClient.MAX_BYTES];

            //read index (int)
            int index = mInputStream.readInt();
            String name = SocketUtils.readStringData(mInputStream);

            LOG.w("readData", "Started reading data for file: " + name);

            File outputFile = new File(listener.getSaveDirectory(), name);
            listener.registerName(outputFile.getAbsolutePath());

            if (!outputFile.getParentFile().exists()) {
                if (!outputFile.getParentFile().mkdirs())
                    throw new SecurityException();
            }

            //read  file size (long)
            long maxBytesAvailable = mInputStream.readLong();
            listener.registerFileSize(maxBytesAvailable);

            long readData = 0;
            DataOutputStream fileOutputStream = new DataOutputStream(new FileOutputStream(outputFile));

            mOutputStream.write(new byte[]{-1});
            mOutputStream.flush();
            mOutputStream.write(ByteBuffer.allocate(4).putInt(index).array());
            mOutputStream.flush();

            int result;
            int lastRate = -1;
            while (readData < maxBytesAvailable) {
                result = mInputStream.read(buffer, 0, (int) Math.min(maxBytesAvailable - readData, A2PClient.MAX_BYTES));
                if (result == -1) {
                    throw new IOException("Error reading header data");
                }

                fileOutputStream.write(buffer, 0, result);
                fileOutputStream.flush();
                readData += result;

                byte rate = (byte) ((readData * 100) / maxBytesAvailable);
                if (rate > lastRate) {
                    lastRate = rate;
                    //this is done at most 101 times, better that way
                    listener.registerProgress(readData);
                    mOutputStream.write(new byte[]{rate});
                    mOutputStream.flush();
                }
            }

            mOutputStream.write(new byte[]{-2});
            mOutputStream.flush();

            fileOutputStream.flush();
            fileOutputStream.close();

            LOG.w("readData", "Read data of size " + readData + " bytes from " + maxBytesAvailable + " bytes");

            listener.registerFinished();

        } catch (SecurityException e) {
            listener.registerErrorMessage("Can't create or access destination folder!");
            listener.registerFinished();
            throw new DirectoryAccessException("Can't create or access destination folder!");
        } catch (IOException e) {
            listener.registerFinished();
            throw e;
        }
    }

    public interface FileTransferListener {

        void registerName(String name);

        void registerFileSize(long fileSize);

        void registerProgress(long transferred);

        void registerErrorMessage(String message);

        void registerFinished();

        File getSaveDirectory();
    }

    public interface DeviceConnectListener {

        /**
         * register device, persist or update if possible, modify JSONObject to send result back to invoker
         *
         * @param object
         */
        DeviceConnection registerDevice(JSONObject object);

        /**
         * Show authentication code on the device screen, this method should be invoked on the UI thread
         *
         */
        void showAuthCode(DeviceConnection connection);
    }
}
