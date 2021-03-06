/*
 * Copyright (C) 2015 The Pure Nexus Project
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
package com.android.settings.nexus;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import com.android.settings.preference.SystemSettingSwitchPreference;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class NavBarSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "NavBarSettings";

    // kill-app long press back
    private static final String KILL_APP_LONGPRESS_BACK = "kill_app_longpress_back";
    // Naviigation bar left
    private static final String KEY_NAVIGATION_BAR_LEFT = "navigation_bar_left";
    private static final String KEY_NAVIGATION_KEY_COLOR = "navbar_key_color";

    private static final String CATEGORY_NAVBAR = "navigation_bar";
    private static final String CATEGORY_NAVBARC = "navigation_barc";

    // kill-app long press back
    private SwitchPreference mKillAppLongPressBack;
    private ColorPickerPreference mNavKeyColor;
    private SystemSettingSwitchPreference mNavBarEnable;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.nexus_navbar_settings);

        final Resources res = getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        final PreferenceCategory navbarCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_NAVBAR);
        final PreferenceCategory navbarCategoryC =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_NAVBARC);

        // kill-app long press back
        mKillAppLongPressBack = (SwitchPreference) findPreference(KILL_APP_LONGPRESS_BACK);
        mKillAppLongPressBack.setOnPreferenceChangeListener(this);
        int killAppLongPressBack = Settings.Secure.getInt(getContentResolver(),
                KILL_APP_LONGPRESS_BACK, 0);
        mKillAppLongPressBack.setChecked(killAppLongPressBack != 0);

        // Enable or disable NavbarImeSwitcher based on boolean: config_show_cmIMESwitcher
        boolean showCmImeSwitcher = getResources().getBoolean(
                com.android.internal.R.bool.config_show_cmIMESwitcher);
        if (!showCmImeSwitcher) {
            Preference pref = findPreference(Settings.System.STATUS_BAR_IME_SWITCHER);
            if (pref != null) {
                navbarCategory.removePreference(pref);
            }
        }

        if (!Utils.isPhone(getActivity())) {
            navbarCategory.removePreference(
                    findPreference(Settings.System.NAVBAR_LEFT_IN_LANDSCAPE));
        }

        mNavBarEnable = (SystemSettingSwitchPreference) prefScreen.findPreference("navigation_bar_show");

        mNavKeyColor = (ColorPickerPreference) findPreference(KEY_NAVIGATION_KEY_COLOR);
        mNavKeyColor.setOnPreferenceChangeListener(this);
        mNavKeyColor.setSummary(mNavKeyColor.getSummaryText() + ColorPickerPreference.convertToARGB(Settings.System.getInt(resolver,
                     Settings.System.NAVIGATION_KEY_COLOR, mNavKeyColor.getPrefDefault())));
        mNavKeyColor.setNewPreviewColor(Settings.System.getInt(resolver, Settings.System.NAVIGATION_KEY_COLOR, mNavKeyColor.getPrefDefault()));

        mNavKeyColor.setPreviewDim(mNavBarEnable.isChecked());

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {

        // kill-app long press back
        if (preference == mKillAppLongPressBack) {
            boolean value = (Boolean) objValue;
            Settings.Secure.putInt(getContentResolver(), KILL_APP_LONGPRESS_BACK,
                    value ? 1 : 0);
            return true;
        } else if (preference == mNavKeyColor) {
            Settings.System.putInt(getActivity().getContentResolver(), Settings.System.NAVIGATION_KEY_COLOR, (Integer) objValue);
            preference.setSummary(((ColorPickerPreference) preference).getSummaryText() + ColorPickerPreference.convertToARGB((Integer) objValue));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
        if (preference == mNavBarEnable) {
            mNavKeyColor.setPreviewDim(mNavBarEnable.isChecked());
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
        return false;
    }
}
