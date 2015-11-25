package android.olayinka.file.transfer.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.olayinka.file.transfer.AppSettings;
import android.olayinka.file.transfer.Utils;

/**
 * Created by Olayinka on 11/17/2015.
 */
public abstract class NetworkStateBroadcastReceiver extends BroadcastReceiver {
    public static final String STATE_POSSIBLE_WIFI = "state.possible.wifi";
    public static final String STATE_POSSIBLE_HOTSPOT = "state.possible.hotspot";
    public static final String STATE_IMPOSSIBLE_WIFI = "state.impossible.wifi";
    public static final String STATE_IMPOSSIBLE = "state.impossible";
    ConnectivityManager mConnectivityManager;
    private WifiManager mWifiManager;


    public NetworkStateBroadcastReceiver(Context context) {
        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {

        switch (intent.getAction()) {

            case WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION:

                if (intent.hasExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED)) {
                    if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {
                        Utils.toast(context, "EXTRA_SUPPLICANT_CONNECTED: Wifi is connected");
                        publishState(STATE_POSSIBLE_WIFI);
                    } else {
                        if (mWifiManager != null && mWifiManager.isWifiEnabled()) {
                            Utils.toast(context, "EXTRA_SUPPLICANT_CONNECTED: Wifi is enabled but not connected");
                            publishState(STATE_IMPOSSIBLE_WIFI);
                        } else {
                            Utils.toast(context, "EXTRA_SUPPLICANT_CONNECTED: Wifi is not enabled");
                            publishState(STATE_IMPOSSIBLE);
                        }
                    }
                }
                break;

            case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (networkInfo.isConnected()) {
                    Utils.toast(context, "EXTRA_NETWORK_INFO: Wifi is connected");
                    publishState(STATE_POSSIBLE_WIFI);
                } else {
                    if (mWifiManager != null && mWifiManager.isWifiEnabled()) {
                        Utils.toast(context, "EXTRA_NETWORK_INFO: Wifi is enabled but not connected");
                        publishState(STATE_IMPOSSIBLE_WIFI);
                    } else {
                        Utils.toast(context, "EXTRA_NETWORK_INFO: Wifi is not enabled");
                        publishState(STATE_IMPOSSIBLE);
                    }
                }
                break;
            case WifiManager.WIFI_STATE_CHANGED_ACTION:

                if (intent.hasExtra(WifiManager.EXTRA_WIFI_STATE)) {
                    switch (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED)) {
                        case WifiManager.WIFI_STATE_ENABLED:
                            final NetworkInfo ni = mConnectivityManager.getActiveNetworkInfo();
                            if (ni != null && ni.isConnected()) {
                                Utils.toast(context, "EXTRA_WIFI_STATE: Wifi is connected");
                                publishState(STATE_POSSIBLE_WIFI);
                            } else {
                                Utils.toast(context, "EXTRA_WIFI_STATE: Wifi is enabled but not connected");
                                publishState(STATE_IMPOSSIBLE_WIFI);
                            }
                            break;
                        default:
                            Utils.toast(context, "EXTRA_WIFI_STATE: Wifi is not enabled");
                            publishState(STATE_IMPOSSIBLE);
                            break;
                    }
                }
                break;
            case AppSettings.WIFI_AP_STATE_ACTION:
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                if (WifiManager.WIFI_STATE_ENABLED == state % 10) {
                    Utils.toast(context, "EXTRA_WIFI_STATE: Hotspot is enabled");
                    publishState(STATE_POSSIBLE_HOTSPOT);
                } else {
                    if (mWifiManager != null && mWifiManager.isWifiEnabled()) {
                        final NetworkInfo ni = mConnectivityManager.getActiveNetworkInfo();
                        if (ni != null && ni.isConnected()) {
                            Utils.toast(context, "EXTRA_WIFI_STATE: Hotspot is not enabled. Wifi is connected");
                            publishState(STATE_POSSIBLE_WIFI);
                        } else {
                            Utils.toast(context, "EXTRA_WIFI_STATE: Hotspot is not enabled. Wifi is enabled but not connected");
                            publishState(STATE_IMPOSSIBLE_WIFI);
                        }
                    } else {
                        Utils.toast(context, "EXTRA_WIFI_STATE: Hotspot is not enabled. Wifi is not enabled");
                        publishState(STATE_IMPOSSIBLE);
                    }

                }
        }
    }

    public abstract void publishState(String state);
}
