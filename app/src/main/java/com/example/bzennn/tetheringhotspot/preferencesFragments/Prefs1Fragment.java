package com.example.bzennn.tetheringhotspot.preferencesFragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.example.bzennn.tetheringhotspot.R;

public class Prefs1Fragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceClickListener {
    public static final String APP_PREFERENCES = "tethering_hotspot_prefs";
    public static final String APP_PREFERENCES_TTL = "ttl_value";

    public int tempTTLValue = 63;

    private PreferenceManager preferenceManager;
    private SharedPreferences sharedPreferences;
    private CheckBoxPreference checkBoxPreference;
    private EditTextPreference editTextPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.app_preferences);

        //preferenceManager.setSharedPreferencesName(APP_PREFERENCES);
        //preferenceManager.setSharedPreferencesMode(Context.MODE_PRIVATE);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        preferecesEnabled();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals("check_box_preference_1")){
            if(sharedPreferences.getBoolean("check_box_preference_1", true)){
                tempTTLValue = 63;
            } else {
                tempTTLValue = 127;
            }
        }

        if(key.equals("check_box_preference_2")){
            if(sharedPreferences.getBoolean("check_box_preference_2", true)){
                findPreference("check_box_preference_1").setEnabled(false);
                findPreference("edit_text_preference_1").setEnabled(true);
            } else {
                findPreference("check_box_preference_1").setEnabled(true);
                findPreference("edit_text_preference_1").setEnabled(false);
            }
        }

        if(key.equals("edit_text_preference_1")){
            EditTextPreference preference = (EditTextPreference) findPreference("edit_text_preference_1");
            tempTTLValue = Integer.parseInt(preference.getText());

        }

    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(APP_PREFERENCES_TTL, tempTTLValue);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    private void preferecesEnabled(){
        if(sharedPreferences.getBoolean("check_box_preference_2", true)){
            findPreference("check_box_preference_1").setEnabled(false);
            findPreference("edit_text_preference_1").setEnabled(true);
        } else {
            findPreference("check_box_preference_1").setEnabled(true);
            findPreference("edit_text_preference_1").setEnabled(false);
        }
    }
}
