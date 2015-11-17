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

package android.olayinka.file.transfer.service.send;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.olayinka.file.transfer.AppSettings;
import android.olayinka.file.transfer.AppSqlHelper;
import android.olayinka.file.transfer.AsyncTask;
import android.olayinka.file.transfer.Utils;
import android.support.v7.app.NotificationCompat;
import android.text.InputType;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.olayinka.file.transfer.AbstractAppSettings;
import com.olayinka.file.transfer.R;
import com.olayinka.file.transfer.content.AbstractAppContent;
import com.olayinka.file.transfer.exception.FileTransferException;
import com.olayinka.file.transfer.model.Device;
import ripped.android.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;

import static com.olayinka.file.transfer.SocketUtils.writeStringData;


/**
 * Created by Olayinka on 8/15/2015.
 */
public class A2PClient extends AsyncTask<Uri, String, Integer> {

    private Device mDevice;
    private Context mContext;
    private DataOutputStream mDataOutputStream;
    private DataInputStream mDataInputStream;
    private ProgressTask mProgressTask;
    private Socket mSocket;
    private Uri[] mUris;
    private boolean[] mStatuses;
    private boolean mCancelRequest;
    private String mAuthCode = null;
    private JSONObject mThisDevice;
    private int mAuthCounter = 0;

    public A2PClient(Context mContext, Device mDevice) {
        this.mContext = mContext;
        this.mDevice = mDevice;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mNotifBuilder = new NotificationCompat.Builder(mContext);
        mNotifBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mNotifBuilder.setContentTitle(mContext.getString(R.string.app_name));
    }

    @Override
    protected final Integer doInBackground(Uri... params) {
        initThisDevice();

        mUris = params;

        try {

            publishProgress(android.olayinka.file.transfer.AppSettings.PROGRESS_INIT);

            try {
                initConnection();
            } catch (IOException e) {
                e.printStackTrace();
                return AbstractAppSettings.RETURN_CONNECTION_LOST;
            }

            publishProgress(android.olayinka.file.transfer.AppSettings.PROGRESS_CONNECT_SUCCESS);

            byte authAction = AppSettings.AUTH_FAILED;
            while (!mCancelRequest && mAuthCounter < 3 && authAction != AppSettings.AUTH_SUCCESS) {
                try {
                    authAction = requestAuthentication();
                    mAuthCounter++;
                } catch (IOException e) {
                    e.printStackTrace();
                    return AbstractAppSettings.RETURN_CONNECTION_LOST;
                }
            }

            if (mCancelRequest || authAction != AppSettings.AUTH_SUCCESS) {
                return AbstractAppSettings.RETURN_AUTH_FAILED;
            }

            publishProgress(android.olayinka.file.transfer.AppSettings.PROGRESS_AUTH_SUCCESS);


            for (int index = 0; index < mUris.length; index++) {
                try {
                    sendStartNotifierToDevice();
                } catch (IOException e) {
                    e.printStackTrace();
                    return AbstractAppSettings.RETURN_CONNECTION_LOST;
                }
                Uri uri = mUris[index];

                //get and verify file availability
                File file = new File(uri.getPath());
                if (!file.exists() || !file.canRead()) {
                    mProgressTask.expectedFiles--;
                    publishProgress(android.olayinka.file.transfer.AppSettings.PROGRESS_FILE_ERROR);
                    continue;
                }

                try {
                    sendFileInfo(file, index);
                } catch (IOException e) {
                    e.printStackTrace();
                    return AbstractAppSettings.RETURN_CONNECTION_LOST;
                }

                try {
                    sendFileData(file);
                } catch (IOException e) {
                    e.printStackTrace();
                    return AbstractAppSettings.RETURN_FAILED_FILE;
                }
            }

            try {
                sendEndNotifierToDevice();
            } catch (IOException e) {
                e.printStackTrace();
                return AbstractAppSettings.RETURN_CONNECTION_LOST;
            }

            //Wait for progress confirmation
            //noinspection StatementWithEmptyBody
            while (mProgressTask != null && !mProgressTask.done) ;
        } finally {
            cleanUp();
        }


        return AbstractAppSettings.RETURN_TRANSFER_SUCCESS;
    }

    private void initThisDevice() {
        mThisDevice = new JSONObject();

        //Get name of this device
        AppSqlHelper sqlHelper = AppSqlHelper.instance(mContext);
        SQLiteDatabase database = sqlHelper.getReadableDatabase();
        Cursor cursor = database.query(AppSqlHelper.APP_INFO, null, null, null, null, null, null);
        cursor.moveToNext();
        String deviceName = cursor.getString(1);
        cursor.close();

        //Get mac address of this device and hash it for fuck's sake
        WifiManager manager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        String address = null;
        try {
            address = Utils.hash(info.getMacAddress());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        mThisDevice.put(AbstractAppContent.DeviceColumns.DEVICE_TYPE, AppSettings.DEVICE_TYPE.toString());
        mThisDevice.put(AbstractAppContent.DeviceColumns.NAME, deviceName);
        mThisDevice.put(Device.Columns.MAC_ADDRESS, address);
    }

    private byte requestAuthentication() throws IOException {
        mDataOutputStream.write(new byte[]{AbstractAppSettings.MESSAGE_INIT_CONNECT});
        mDataOutputStream.flush();

        mThisDevice.put(AbstractAppContent.DeviceColumns.AUTH_HASH, mDevice.getAuthHash());

        writeStringData(mDataOutputStream, mThisDevice.toString());

        byte authAction = mDataInputStream.readByte();

        if (authAction == AbstractAppSettings.AUTH_FAILED) {

            mCancelRequest = false;

            publishProgress(AbstractAppSettings.PROGRESS_REQUEST_AUTH);

            //noinspection StatementWithEmptyBody
            while (mAuthCode == null && !mCancelRequest) ;

            if (mCancelRequest) return authAction;

            try {
                String authHash = Utils.hash(mAuthCode);
                mDevice.setAuthHash(authHash);
                if (!AppSqlHelper.instance(mContext).getDeviceProvider().updateDevice(mDevice)) {
                    mCancelRequest = true;
                }
                mAuthCode = null;
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                mCancelRequest = true;
            }
        } else if (authAction == AbstractAppSettings.AUTH_ACCESS_DENIED) {
            mCancelRequest = true;
        }
        return authAction;
    }

    private void sendEndNotifierToDevice() throws IOException {
        mDataOutputStream.write(new byte[]{AbstractAppSettings.MESSAGE_FILE_TRANSFER_EXIT});
        mDataOutputStream.flush();
    }

    private void sendStartNotifierToDevice() throws IOException {
        mDataOutputStream.write(new byte[]{AbstractAppSettings.MESSAGE_FILE_TRANSFER_ENTRY});
        mDataOutputStream.flush();
    }

    private void cleanUp() {
        if (mProgressTask != null) {
            mProgressTask.cancel(true);
        }
        if (mSocket == null)
            return;
        try {
            mDataOutputStream.flush();
            mDataOutputStream.close();
            mDataInputStream.close();
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mSocket = null;
        mDataInputStream = null;
        mDataOutputStream = null;
    }

    @SuppressWarnings("DuplicateThrows")
    private void sendFileData(File file) throws FileNotFoundException, IOException, FileTransferException {
        FileInputStream fileInputStream = new FileInputStream(file);
        long fileSize = file.length();
        String fileName = file.getName();
        byte[] buffer = new byte[AbstractAppSettings.BUFFER_SIZE];
        int result = 0;
        long total = 0;
        while (result != -1) {
            result = fileInputStream.read(buffer);
            if (result != -1) {
                mDataOutputStream.write(buffer, 0, result);
                mDataOutputStream.flush();
                total += result;
            } else if (total != fileSize) {
                throw new FileTransferException("Error writing file " + fileName + " Transfer failed, please try again");
            }
        }
    }

    //send index (int), file name length (int), file name(byte[]), file size (long)
    private void sendFileInfo(File file, int index) throws IOException {
        mDataOutputStream.write(ByteBuffer.allocate(4).putInt(index).array());
        mDataOutputStream.flush();
        writeStringData(mDataOutputStream, file.getName());
        mDataOutputStream.write(ByteBuffer.allocate(8).putLong(file.length()).array());
        mDataOutputStream.flush();
    }

    private void initConnection() throws IOException {
        mSocket = new Socket(mDevice.getLastKnownIp(), AbstractAppSettings.PORT);
        mDataOutputStream = new DataOutputStream(mSocket.getOutputStream());
        mDataInputStream = new DataInputStream(mSocket.getInputStream());
    }

    NotificationCompat.Builder mNotifBuilder;

    @Override
    protected void onProgressUpdate(String... values) {
        String action = values[0];
        int notificationId;
        String contentText;
        switch (action) {
            case android.olayinka.file.transfer.AppSettings.PROGRESS_AUTH_SUCCESS:
                notificationId = android.olayinka.file.transfer.AppSettings.NOTIF_AUTH_SUCCESS;
                contentText = mContext.getString(R.string.notif_auth_success);
                mProgressTask = new ProgressTask();
                mProgressTask.executeNow();
                break;
            case android.olayinka.file.transfer.AppSettings.PROGRESS_FILE_ERROR:
                notificationId = android.olayinka.file.transfer.AppSettings.NOTIF_FILE_ERROR;
                contentText = mContext.getString(R.string.notif_file_error);
                break;
            case android.olayinka.file.transfer.AppSettings.PROGRESS_CONNECT_SUCCESS:
                notificationId = android.olayinka.file.transfer.AppSettings.NOTIF_CONNECT_SUCCESS;
                contentText = mContext.getString(R.string.notif_connect_success);
                break;
            case android.olayinka.file.transfer.AppSettings.PROGRESS_INIT:
                notificationId = android.olayinka.file.transfer.AppSettings.NOTIF_INIT;
                contentText = mContext.getString(R.string.notif_init);
                break;
            case android.olayinka.file.transfer.AppSettings.PROGRESS_REQUEST_AUTH:
                notificationId = android.olayinka.file.transfer.AppSettings.NOTIF_REQUEST_AUTH;
                contentText = mContext.getString(R.string.notif_request_auth);
                final TextView textView = new EditText(mContext);
                AlertDialog dialog = new AlertDialog.Builder(mContext)
                        .setMessage("Enter authentication code shown on the device ( " + mDevice.getName() + " ).")
                        .setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mAuthCode = textView.getText().toString().trim();
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCancelRequest = true;
                            }
                        })
                        .setTitle(mDevice.getName())
                        .setView(textView)
                        .setCancelable(false)
                        .create();
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                dialog.show();
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins((int) android.olayinka.file.transfer.Utils.pxFromDp(mContext, 30f), (int) android.olayinka.file.transfer.Utils.pxFromDp(mContext, 30f), (int) android.olayinka.file.transfer.Utils.pxFromDp(mContext, 30f), (int) android.olayinka.file.transfer.Utils.pxFromDp(mContext, 30f));
                textView.setLayoutParams(params);
                textView.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            default:
                return;
        }
        mNotifBuilder.setTicker(contentText);
        mNotifBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));
        mNotifBuilder.setContentText(contentText);
        notifyUser(notificationId);
        super.onProgressUpdate(values);
    }

    private void notifyUser(int notificationId) {
        Notification notification = mNotifBuilder.build();
        //notification.flags = notification.flags | Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL | Notification.FLAG_NO_CLEAR;
        NotificationManager mNotifyMgr = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(notificationId, notification);
    }

    @Override
    protected void onPostExecute(Integer returnCode) {
        if (returnCode != null) {
            String[] contentText = null;
            int notificationId = 0x16916;
            mNotifBuilder.setProgress(0, 0, false);
            switch (returnCode) {
                case AppSettings.RETURN_FAILED_FILE:
                    contentText = mContext.getString(R.string.return_failed_file).split("\\|");
                    notificationId = R.string.return_failed_file;
                    break;
                case AppSettings.RETURN_CONNECTION_LOST:
                    contentText = mContext.getString(R.string.return_connection_lost).split("\\|");
                    notificationId = R.string.return_connection_lost;
                    break;
                case AppSettings.RETURN_TRANSFER_SUCCESS:
                    mNotifBuilder.setProgress(100, 100, false);
                    contentText = mContext.getString(R.string.return_transfer_success).split("\\|");
                    notificationId = AppSettings.NOTIF_FILE_PROGRESS;
                    break;
                case AppSettings.RETURN_AUTH_FAILED:
                    contentText = mContext.getString(R.string.return_auth_failed).split("\\|");
                    notificationId = AppSettings.NOTIF_REQUEST_AUTH;
                    break;
            }
            if (contentText != null) {
                mNotifBuilder.setContentText(contentText[1]);
                mNotifBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText[1]));
                mNotifBuilder.setTicker(contentText[1]);
                mNotifBuilder.setContentTitle(contentText[0]);
                notifyUser(notificationId);
            }
        }

        if (mProgressTask != null) {
            mProgressTask.stopFlag = true;
            mProgressTask.cancel(true);
            mProgressTask = null;
        }

        super.onPostExecute(returnCode);
    }

    class ProgressTask extends AsyncTask<Long, Byte, Integer> {

        private long lastUpdate;
        private boolean stopFlag = false;
        private boolean done = false;
        private int currentIndex = -1;
        private File currentFile;
        private int expectedFiles = mUris.length;
        private int readFiles = 0;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            lastUpdate = System.currentTimeMillis();
        }

        @Override
        protected Integer doInBackground(Long... params) {

            while (!isCancelled() && !stopFlag) {
                try {
                    byte progress = mDataInputStream.readByte();
                    if (progress == android.olayinka.file.transfer.AppSettings.FILE_PROGRESS_START) {
                        currentIndex = mDataInputStream.readInt();
                        currentFile = new File(mUris[currentIndex].getPath());
                    } else if (progress == android.olayinka.file.transfer.AppSettings.FILE_PROGRESS_END) {
                        readFiles++;
                        mStatuses[currentIndex] = true;
                    } else if (progress == AbstractAppSettings.MESSAGE_FILE_TRANSFER_EXIT)
                        return 0;
                    else
                        publishProgress(progress);
                } catch (IOException e) {
                    e.printStackTrace();
                    if (readFiles == expectedFiles) {
                        return 0;
                    }
                    return AbstractAppSettings.PROGRESS_FAILED;
                } catch (NullPointerException ex) {
                    if (currentIndex == -1) return AppSettings.PROGRESS_FAILED;
                }
            }
            return null;
        }


        @Override
        protected void onProgressUpdate(Byte... values) {
            byte aByte = values[0];
            if (System.currentTimeMillis() - lastUpdate >= 500 || aByte == 100) {
                lastUpdate = System.currentTimeMillis();
                mNotifBuilder.setProgress(100, aByte, false);
                mNotifBuilder.setContentTitle("Transferring " + (currentIndex + 1) + "/" + mUris.length);
                mNotifBuilder.setContentText(currentFile.getName());
                mNotifBuilder.setTicker(currentFile.getName());
                mNotifBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("Transferring " + currentFile.getName() + " to " + android.olayinka.file.transfer.Utils.buildDeviceName(mDevice)));
                notifyUser(android.olayinka.file.transfer.AppSettings.NOTIF_FILE_PROGRESS);
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            done = true;
        }
    }

}
