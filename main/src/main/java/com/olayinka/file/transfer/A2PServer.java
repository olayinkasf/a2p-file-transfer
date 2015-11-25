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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Olayinka on 7/27/2015.
 */
public abstract class A2PServer {

    public static final String CLIENT_ACCEPT_FAIL = "init.fail";
    public static final String SERVER_INIT_SUCCESS = "server.init.success";
    public static final String SERVER_EXIT_ERROR = "server.exit.error";
    public static final String SERVER_EXIT_SUCCESS = "server.exit.success";
    public static final String CLIENT_READ_FAIL = "client.read.fail";
    final HashMap<Integer, A2PClient> mClients = new HashMap<>(20);
    final HashMap<Integer, Boolean> mClientsStatus = new HashMap<>(20);
    protected List<A2PServerListener> mListener;
    ServerSocket mServerSocket;
    private boolean mDisconnect = false;
    private A2PClient.ListenerProvider mListenerProvider;

    public A2PServer(A2PServerListener... listener) {
        mListener = new ArrayList<>(10);
        mListener.addAll(Arrays.asList(listener));
    }

    public void run() {
        try {
            mServerSocket = new ServerSocket(AbstractAppSettings.PORT);
            mServerSocket.setSoTimeout(0);
        } catch (IOException e) {
            e.printStackTrace();
            dispatchExitMessage(CLIENT_ACCEPT_FAIL, "Couldn't create server!", e);
            mDisconnect = true;
            return;
        }

        dispatchMessage(SERVER_INIT_SUCCESS, "File transfer service is active.", null);

        while (!mDisconnect) {
            try {
                Socket client = mServerSocket.accept();
                initializeCommunication(client);
            } catch (java.net.SocketException e) {
                if (!mDisconnect)
                    dispatchExitMessage(SERVER_EXIT_ERROR, "An error was encountered. Transfer service is no longer active.", e);
                mDisconnect = true;
                break;
            } catch (IOException e) {
                e.printStackTrace();
                if (!mDisconnect)
                    dispatchMessage(CLIENT_ACCEPT_FAIL, "Couldn't connect to client. Please try reconnecting.", e);
            } catch (Exception ignored) {
                if (!mDisconnect)
                    dispatchMessage(CLIENT_ACCEPT_FAIL, "Couldn't connect to client. Please try reconnecting.", ignored);
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        mDisconnect = true;
        disconnect();
    }

    public void dispatchExitMessage(String code, String message, Exception e) {
        if (mListener != null) {
            for (A2PServerListener listener : mListener)
                listener.exit(code, message, e);
        }
    }

    public void dispatchMessage(String code, String message, Exception e) {
        if (mListener != null) {
            for (A2PServerListener listener : mListener)
                listener.message(code, message, e);
        }
    }

    private void initializeCommunication(Socket client) throws IOException {
        if (mDisconnect)
            return;
        A2PClient a2PClient = new A2PClient(this, client);
        synchronized (mClients) {
            mClients.put(a2PClient.getId(), a2PClient);
        }
        a2PClient.setListenerProvider(mListenerProvider);
        startClient(a2PClient);
    }

    protected abstract void startClient(A2PClient a2PClient);

    public void disconnect() {
        LOG.w("disconnect", "Received request to disconnect.");
        mDisconnect = true;
        List<A2PClient> clients = new ArrayList<>(mClients.values());

        for (A2PClient client : clients) {
            removeCommunication(client);
        }
        mClients.clear();
        if (!mServerSocket.isClosed()) {
            try {
                mServerSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
                dispatchExitMessage(SERVER_EXIT_ERROR, "Error closing the server socket.", e);
                return;
            }
        }
        dispatchExitMessage(SERVER_EXIT_SUCCESS, "Server successfully closed", null);
    }

    public void updateStatus(A2PClient client, boolean status) {
        synchronized (mClientsStatus) {
            Boolean oldStats = mClientsStatus.get(client.getId());
            if (mDisconnect) {
                removeCommunication(client);
            } else if (oldStats == null) {
                mClientsStatus.put(client.getId(), status);
                LOG.w("acceptConnection", "Communication initialized for client at " + client.getSocket().getInetAddress().getHostName() + " with id " + client.getId());
            } else if (!oldStats) {
                LOG.e("connection", "We have a problem here! This shouldn't happen!");
            } else {
                if (!status) {
                    LOG.w("connection", "Disconnecting from client at " + client.getSocket().getInetAddress().getHostName() + " with id " + client.getId());
                    removeCommunication(client);
                }
            }
        }
    }

    private void removeCommunication(A2PClient client) {
        synchronized (mClientsStatus) {
            synchronized ((mClients)) {
                mClientsStatus.remove(client.getId());
                mClients.remove(client.getId());
            }
        }
    }

    public void setListenerProvider(A2PClient.ListenerProvider listenerProvider) {
        this.mListenerProvider = listenerProvider;
    }

    public boolean isActive() {
        return !mDisconnect;
    }

    public interface Interface {
        void setListenerProvider(A2PClient.ListenerProvider listenerProvider);

        void disconnect();

        boolean isActive();
    }
}

