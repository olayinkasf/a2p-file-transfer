package android.olayinka.file.transfer.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.olayinka.file.transfer.AppSettings;
import android.olayinka.file.transfer.AsyncTask;
import android.olayinka.file.transfer.Utils;
import android.olayinka.file.transfer.service.send.A2PClient;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.olayinka.file.transfer.R;
import com.olayinka.file.transfer.model.Device;
import ripped.android.json.JSONObject;

import java.io.*;

/**
 * Created by Olayinka on 11/25/2015.
 */
public class QRCodeActivity extends AppCompatActivity {

    private static final int WHITE = 0x00FFFFFF;
    private static final int BLACK = 0xFF000000;

    private JSONObject mThisDevice;
    private DisplayMetrics mDimens;


    void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.device_info);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        initToolbar();
        mThisDevice = A2PClient.initThisDevice(this);
        mDimens = Utils.displayDimens(this);
        setQRImage();

        TextView deviceName = (TextView) findViewById(R.id.deviceName);
        TextView ipId = (TextView) findViewById(R.id.ipId);

        deviceName.setText(mThisDevice.getString(Device.Columns.NAME));
        ipId.setText("@" +
                mThisDevice.getString(Device.Columns.LAST_KNOWN_IP) + "/"
                + mThisDevice.getString(Device.Columns.MAC_ADDRESS).substring(0, 16).toUpperCase());

    }

    public void setQRImage() {

        WifiManager wifiMan = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        int ipAddress = wifiInf.getIpAddress();
        String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        mThisDevice.put(Device.Columns.LAST_KNOWN_IP, ip);
        if (ip.equals("0.0.0.0")) {
            Utils.toast(this, R.string.not_connected);
            mThisDevice.put(Device.Columns.LAST_KNOWN_IP, ip);
            return;
        }

        String lastEncoded = getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE).getString(AppSettings.LAST_ENCODED, null);
        if (lastEncoded != null && lastEncoded.equals(mThisDevice.toString())) {
            setQRImageFromLocalDir();
            return;
        }

        new AsyncTask<Void, Bitmap, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                int qrDimens = Math.min(mDimens.widthPixels, mDimens.heightPixels);
                BitMatrix result = null;
                try {
                    result = new MultiFormatWriter().encode(Utils.jsonArrayOfDevice(mThisDevice).toString(),
                            BarcodeFormat.QR_CODE, qrDimens, qrDimens, null);
                } catch (WriterException e) {
                    e.printStackTrace();
                    return null;
                }

                int width = result.getWidth();
                int height = result.getHeight();
                int[] pixels = new int[width * height];
                for (int y = 0; y < height; y++) {
                    int offset = y * width;
                    for (int x = 0; x < width; x++) {
                        pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
                    }
                }

                Bitmap bitmap = Bitmap.createBitmap(qrDimens, qrDimens, Bitmap.Config.ARGB_8888);
                bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

                publishProgress(bitmap);
                getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE)
                        .edit().putString(AppSettings.LAST_ENCODED, mThisDevice.toString()).apply();

                OutputStream fOut = null;
                try {
                    fOut = new FileOutputStream(getQRFile());
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fOut != null) {
                            fOut.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (fOut != null) {
                            fOut.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(Bitmap... values) {
                setBitmapImage(values[0]);
            }
        }.executeNow();

    }

    private void setQRImageFromLocalDir() {
        new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... params) {
                File imgFile = getQRFile();
                if (imgFile.exists()) {
                    return BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                setBitmapImage(bitmap);
            }
        }.executeNow();

    }

    private void setBitmapImage(Bitmap bitmap) {
        ImageView myImage = (ImageView) findViewById(R.id.deviceInfo);
        myImage.setImageBitmap(bitmap);
    }

    File getQRFile() {
        File path = getFilesDir();
        path = new File(path, "qr-code");
        path.mkdirs();
        path = new File(path, "qr-code.cache");
        return path;
    }
}