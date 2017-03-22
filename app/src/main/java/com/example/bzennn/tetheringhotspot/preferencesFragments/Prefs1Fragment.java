package com.example.bzennn.tetheringhotspot.preferencesFragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.example.bzennn.tetheringhotspot.R;

public class Prefs1Fragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.app_preferences);


        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        preferecesEnabledOnStart();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals("check_box_preference_2")) {
            if(sharedPreferences.getBoolean("check_box_preference_2", true)) {
                findPreference("check_box_preference_1").setEnabled(false);
                findPreference("edit_text_preference_1").setEnabled(true);
            } else {
                findPreference("check_box_preference_1").setEnabled(true);
                findPreference("edit_text_preference_1").setEnabled(false);
            }
        }

        if(key.equals("check_box_preference_4")) {
            if(sharedPreferences.getBoolean("check_box_preference_4", true)) {
                findPreference("check_box_preference_5").setEnabled(true);
            } else {
                findPreference("check_box_preference_5").setEnabled(false);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    private void preferecesEnabledOnStart() {
        if(sharedPreferences.getBoolean("check_box_preference_2", true)) {
            findPreference("check_box_preference_1").setEnabled(false);
            findPreference("edit_text_preference_1").setEnabled(true);
        } else {
            findPreference("check_box_preference_1").setEnabled(true);
            findPreference("edit_text_preference_1").setEnabled(false);
        }

        if(sharedPreferences.getBoolean("check_box_preference_4", true)) {
            findPreference("check_box_preference_5").setEnabled(true);
        } else {
            findPreference("check_box_preference_5").setEnabled(false);
        }
    }
}
