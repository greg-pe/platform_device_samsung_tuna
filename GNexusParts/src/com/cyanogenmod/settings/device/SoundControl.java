/*
 * Copyright (C) 2011 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyanogenmod.settings.device;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Button;
import android.util.Log;
import android.os.Vibrator;

/**
 * Special preference type that allows configuration of sound control settings on Nexus
 * Devices
 */
public class SoundControl extends DialogPreference  implements OnClickListener {

    private static final String TAG = "SOUND...";

    private static final int[] SEEKBAR_ID = new int[] {
            R.id.sound_seekbar
    };

    private static final int[] VALUE_DISPLAY_ID = new int[] {
            R.id.sound_value
    };

    private static final String[] FILE_PATH = new String[] {
            "/sys/devices/virtual/misc/soundcontrol/volume_boost",
    };

    private soundSeekBar mSeekBars[] = new soundSeekBar[1];

    private static final int MAX_VALUE = 3;

    private static final int OFFSET_VALUE = 0;

    private static int sInstances = 0;

    public SoundControl(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.preference_dialog_sound_control);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        sInstances++;

        for (int i = 0; i < SEEKBAR_ID.length; i++) {
            SeekBar seekBar = (SeekBar) view.findViewById(SEEKBAR_ID[i]);
            TextView valueDisplay = (TextView) view.findViewById(VALUE_DISPLAY_ID[i]);
            if (i < 3)
                mSeekBars[i] = new soundSeekBar(seekBar, valueDisplay, FILE_PATH[i], OFFSET_VALUE, MAX_VALUE);
            else
                mSeekBars[i] = new soundSeekBar(seekBar, valueDisplay, FILE_PATH[i], 0, 3);
        }
        SetupButtonClickListeners(view);
    }

    private void SetupButtonClickListeners(View view) {
            Button mDefaultButton = (Button)view.findViewById(R.id.btnmin);
            mDefaultButton.setOnClickListener(this);

            Button mTestButton = (Button)view.findViewById(R.id.btnmax);
            mTestButton.setOnClickListener(this);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        sInstances--;

        if (positiveResult) {
            for (soundSeekBar csb : mSeekBars) {
                csb.save();
            }
        } else if (sInstances == 0) {
            for (soundSeekBar csb : mSeekBars) {
                csb.reset();
            }
        }
    }

    /**
     * Restore default sound state from SharedPreferences. (Write to kernel.)
     *
     * @param context The context to read the SharedPreferences from
     */
    public static void restore(Context context) {
        if (!isSupported()) {
            return;
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        Boolean bFirstTime = sharedPrefs.getBoolean("FirstVolumeBoost", true);
        for (String filePath : FILE_PATH) {
            String sDefaultValue = Utils.readOneLine(filePath);
            int iValue = sharedPrefs.getInt(filePath, Integer.valueOf(sDefaultValue));
            if (bFirstTime){
                Utils.writeValue(filePath, "0");
                Log.d(TAG, "restore default value: 0 File: " + filePath);
            }
            else{
                Utils.writeValue(filePath, String.valueOf((long) iValue));
                Log.d(TAG, "restore: iValue: " + iValue + " File: " + filePath);
            }
        }
        if (bFirstTime) {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putBoolean("FirstVolumeBoost", false);
            editor.commit();
        }
    }

    /**
     * Check whether the running kernel high performance sound tuning or not.
     *
     */
    public static boolean isSupported() {
        boolean supported = true;
        for (String filePath : FILE_PATH) {
            if (!Utils.fileExists(filePath)) {
                supported = false;
            }
        }

        return supported;
    }

    class soundSeekBar implements SeekBar.OnSeekBarChangeListener {

        private String mFilePath;

        private int mOriginal;

        private SeekBar mSeekBar;

        private TextView mValueDisplay;

        private int iOffset;

        private int iMax;

        public soundSeekBar(SeekBar seekBar, TextView valueDisplay, String filePath, Integer offsetValue, Integer maxValue) {
            int iValue;

            mSeekBar = seekBar;
            mValueDisplay = valueDisplay;
            mFilePath = filePath;
            iOffset = offsetValue;
            iMax = maxValue;

            // Read original value
            if (Utils.fileExists(mFilePath)) {
                String sDefaultValue = Utils.readOneLine(mFilePath);
                iValue = nextBoostValue(Integer.valueOf(sDefaultValue));
            } else {
                iValue = iMax - iOffset;
            }
            mOriginal = iValue;

            mSeekBar.setMax(iMax);

            reset();
            mSeekBar.setOnSeekBarChangeListener(this);
        }

        public void reset() {
            int iValue;

            iValue = mOriginal + iOffset;
            mSeekBar.setProgress(iValue);
            updateValue(mOriginal);
        }

        public void save() {
            int iValue;

            iValue = mSeekBar.getProgress() - iOffset;
            Editor editor = getEditor();
            editor.putInt(mFilePath, nextBoostValue(iValue));
            editor.commit();
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int iValue;

            iValue = progress - iOffset;
            Utils.writeValue(mFilePath, String.valueOf((long) nextBoostValue(iValue)));
            updateValue(iValue);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // Do nothing
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // Do nothing
        }

        private void updateValue(int progress) {
            mValueDisplay.setText(String.format("%d", (int) progress));
        }

        public void setNewValue(int iValue) {
            mOriginal = iValue;
            reset();
        }

        private int minValue(int averageValue) {
            int resultMinValue;

            resultMinValue = 0;
            return resultMinValue;
        }

        private int nextBoostValue(int resultMinValue) {
            int boostValue;

            boostValue = (resultMinValue);
            return boostValue;
        }

    }

    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnmin:
                    setMinValue();
                    break;
            case R.id.btnmax:
                    setMaxValue();
                    break;
        }
    }

    private void setMinValue() {
        mSeekBars[0].setNewValue(0);
    }

    private void setMaxValue() {
        mSeekBars[0].setNewValue(3);
    }
}
