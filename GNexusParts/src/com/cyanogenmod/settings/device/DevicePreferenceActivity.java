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

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import com.cyanogenmod.settings.device.R;

public class DevicePreferenceActivity extends PreferenceFragment {

    public static final String SHARED_PREFERENCES_BASENAME = "com.cyanogenmod.settings.device";
    public static final String ACTION_UPDATE_PREFERENCES = "com.cyanogenmod.settings.device.UPDATE";
    public static final String KEY_COLOR_TUNING = "color_tuning";
    public static final String KEY_GAMMA_TUNING = "gamma_tuning";
    public static final String KEY_COLORGAMMA_PRESETS = "colorgamma_presets";
    public static final String KEY_VIBRATOR_TUNING = "vibrator_tuning";
    public static final String KEY_CATEGORY_RADIO = "category_radio";
    public static final String KEY_HSPA = "hspa";
    public static final String KEY_GPU_OVERCLOCK = "gpu_overclock";
    public static final String KEY_FSYNC_CONTROL = "fsync_control";
    public static final String KEY_SOUND_CONTROL = "sound_control";
    public static final String KEY_HIGHPERFORMANCE_AUDIO = "highperformance_audio";

    private ColorTuningPreference mColorTuning;
    private GammaTuningPreference mGammaTuning;
    private ColorHackPresets mColorHackPresets;
    private VibratorTuningPreference mVibratorTuning;
    private ListPreference mGpuOverclock;
    private SoundControl mSoundControl;
    private ListPreference mFsyncControl;
    private ListPreference mHighPerformanceAudio;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        mColorTuning = (ColorTuningPreference) findPreference(KEY_COLOR_TUNING);
        mColorTuning.setEnabled(ColorTuningPreference.isSupported());

        mGammaTuning = (GammaTuningPreference) findPreference(KEY_GAMMA_TUNING);
        mGammaTuning.setEnabled(GammaTuningPreference.isSupported());

        mColorHackPresets = (ColorHackPresets) findPreference(KEY_COLORGAMMA_PRESETS);
        mColorHackPresets.setEnabled(ColorHackPresets.isSupported());

        mVibratorTuning = (VibratorTuningPreference) findPreference(KEY_VIBRATOR_TUNING);
        mVibratorTuning.setEnabled(VibratorTuningPreference.isSupported());

        mGpuOverclock = (ListPreference) findPreference(KEY_GPU_OVERCLOCK);
        mGpuOverclock.setEnabled(GpuOverclock.isSupported());
        mGpuOverclock.setOnPreferenceChangeListener(new GpuOverclock());
        GpuOverclock.updateSummary(mGpuOverclock, Integer.parseInt(mGpuOverclock.getValue()));

        mSoundControl = (SoundControl) findPreference(KEY_SOUND_CONTROL);
        mSoundControl.setEnabled(SoundControl.isSupported());

        mFsyncControl = (ListPreference) findPreference(KEY_FSYNC_CONTROL);
        mFsyncControl.setEnabled(FsyncControl.isSupported());
        mFsyncControl.setOnPreferenceChangeListener(new FsyncControl());
        FsyncControl.updateSummary(mFsyncControl, Integer.parseInt(mFsyncControl.getValue()));

        mHighPerformanceAudio = (ListPreference) findPreference(KEY_HIGHPERFORMANCE_AUDIO);
        mHighPerformanceAudio.setEnabled(HighPerformanceAudio.isSupported());
        mHighPerformanceAudio.setOnPreferenceChangeListener(new HighPerformanceAudio());
        HighPerformanceAudio.updateSummary(mHighPerformanceAudio, Integer.parseInt(mHighPerformanceAudio.getValue()));
    }
}
