package android.olayinka.file.transfer.service.receive;/*
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


import android.content.Context;
import android.olayinka.file.transfer.AsyncTask;
import com.olayinka.file.transfer.A2PClient;
import com.olayinka.file.transfer.A2PServer;
import com.olayinka.file.transfer.A2PServerListener;

import java.util.List;

/**
 * Created by Olayinka on 7/27/2015.
 */
public class A2PServerTask extends AsyncTask<String, Object, Integer> {

    private static final String PROGRESS_EXIT = "msg.exit";
    private static final String PROGRESS_MSG = "msg.no.exit";
    final Context mContext;
    final A2PServerStub mA2PServer;

    public A2PServerTask(Context context) {
        mA2PServer = new A2PServerStub((A2PServerListener) context);
        this.mContext = context;
    }

    @Override
    protected Integer doInBackground(String... params) {
        mA2PServer.run();
        return null;
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        if (mA2PServer.getListeners() != null) {
            switch ((String) values[0]) {
                case PROGRESS_EXIT:
                    for (A2PServerListener listener : mA2PServer.getListeners())
                        listener.exit((String) values[1], (String) values[2], (Exception) values[3]);
                    break;
                case PROGRESS_MSG:
                    for (A2PServerListener listener : mA2PServer.getListeners())
                        listener.message((String) values[1], (String) values[2], (Exception) values[3]);
                    break;
            }
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
    }

    class A2PServerStub extends A2PServer {

        public A2PServerStub(A2PServerListener context) {
            super(context);
        }

        @Override
        public void dispatchExitMessage(String code, String message, Exception e) {
            publishProgress(PROGRESS_EXIT, code, message, e);
        }

        @Override
        public void dispatchMessage(String code, String message, Exception e) {
            publishProgress(PROGRESS_MSG, code, message, e);
        }

        @Override
        protected void startClient(A2PClient a2PClient) {
            A2PClientTask a2PClientTask = new A2PClientTask(mContext, a2PClient);
            a2PClient.setListenerProvider(a2PClientTask);
            a2PClientTask.executeNow();
        }

        List<A2PServerListener> getListeners() {
            return mListener;
        }

    }


}

