/*
 * Copyright 2015
 *
 * Olayinka S. Folorunso <mail@olayinkasf.com>
 * http://olayinkasf.com
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

package android.olayinka.file.transfer.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import com.olayinka.file.transfer.R;

/**
 * Created by Olayinka on 4/12/2015.
 */
public class PrefsSwitchCompat extends SwitchCompat {

    String mPrefsName;
    String mPrefsKey;

    public PrefsSwitchCompat(Context context) {
        super(context);
    }

    public PrefsSwitchCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.Prefs,
                0, 0);

        try {
            mPrefsName = a.getString(R.styleable.Prefs_prefs_name);
            mPrefsKey = a.getString(R.styleable.Prefs_prefs_key);
        } finally {
            a.recycle();
        }

        setChecked(getContext().getSharedPreferences(mPrefsName, Context.MODE_PRIVATE).getBoolean(mPrefsKey, false));
    }

    public void setPrefs(String name, String key) {
        mPrefsName = name;
        mPrefsKey = key;
        setChecked(getContext().getSharedPreferences(mPrefsName, Context.MODE_PRIVATE).getBoolean(mPrefsKey, false));
    }

    @Override
    public void setChecked(boolean checked) {
        getContext().getSharedPreferences(mPrefsName, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(mPrefsKey, checked)
                .apply();
        super.setChecked(checked);
    }
}
