package android.olayinka.file.transfer.widget;

import android.content.Context;
import android.olayinka.file.transfer.service.NetworkStateBroadcastReceiver;
import android.olayinka.file.transfer.service.send.SendService;
import android.util.AttributeSet;
import android.widget.ImageButton;
import com.olayinka.file.transfer.R;

/**
 * Created by Olayinka on 11/17/2015.
 */
public class ServiceButton extends ImageButton {

    private static final int[] STATE_CONNECTED = {R.attr.state_connected};
    private static final int[] STATE_POSSIBLE_WIFI = {R.attr.state_possible_wifi};
    private static final int[] STATE_POSSIBLE_HOTSPOT = {R.attr.state_possible_hotspot};
    private static final int[] STATE_IMPOSSIBLE_WIFI = {R.attr.state_impossible_wifi};
    private static final int[] STATE_IMPOSSIBLE = {R.attr.state_impossible};
    private String mState = NetworkStateBroadcastReceiver.STATE_IMPOSSIBLE;

    public ServiceButton(Context context) {
        super(context);
        mState = NetworkStateBroadcastReceiver.STATE_IMPOSSIBLE;
    }

    public ServiceButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mState = NetworkStateBroadcastReceiver.STATE_IMPOSSIBLE;
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 5);
        if (mState == null) {
            mergeDrawableStates(drawableState, STATE_IMPOSSIBLE);
            return drawableState;
        }
        switch (mState) {
            case SendService.STATE_CONNECTED:
                mergeDrawableStates(drawableState, STATE_CONNECTED);
                break;
            case NetworkStateBroadcastReceiver.STATE_POSSIBLE_WIFI:
                mergeDrawableStates(drawableState, STATE_POSSIBLE_WIFI);
                break;
            case NetworkStateBroadcastReceiver.STATE_POSSIBLE_HOTSPOT:
                mergeDrawableStates(drawableState, STATE_POSSIBLE_HOTSPOT);
                break;
            case NetworkStateBroadcastReceiver.STATE_IMPOSSIBLE_WIFI:
                mergeDrawableStates(drawableState, STATE_IMPOSSIBLE_WIFI);
                break;
            case NetworkStateBroadcastReceiver.STATE_IMPOSSIBLE:
                mergeDrawableStates(drawableState, STATE_IMPOSSIBLE);
                break;
        }
        return drawableState;
    }

    public String getState() {
        return mState;
    }

    public void setState(String mState) {
        this.mState = mState;
        refreshDrawableState();
    }
}
