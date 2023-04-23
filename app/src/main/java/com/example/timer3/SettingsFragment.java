package com.example.timer3;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.timer_preferences);

        setPreferencesLabels();

        setOnPreferenceChangeListenerToEditTextPreferences();
    }

    private void setOnPreferenceChangeListenerToEditTextPreferences() {
        Preference defaultIntervalEditTextPreference = findPreference("default_interval");
        defaultIntervalEditTextPreference.setOnPreferenceChangeListener(this);

        Preference maximalIntervalEditTextPreference = findPreference("maximal_interval");
        maximalIntervalEditTextPreference.setOnPreferenceChangeListener(this);
    }


    private void setPreferencesLabels() {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        SharedPreferences sharedPreferences = preferenceScreen.getSharedPreferences();
        int count = preferenceScreen.getPreferenceCount();

        for (int i = 0; i < count; i++) {
            Preference preference = preferenceScreen.getPreference(i);

            if (!(preference instanceof CheckBoxPreference)) {
                setLabelToPreferenceWithStringValue(preference, sharedPreferences);
            }
        }
    }

    private void setLabelToPreferenceWithStringValue(Preference preference, SharedPreferences sharedPreferences) {
        String value = sharedPreferences.getString(preference.getKey(), "");
        preference.setSummary(value);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);

        if (!(preference instanceof CheckBoxPreference)) {
            setLabelToPreferenceWithStringValue(preference, sharedPreferences);
        }

        else if (key.equals("enable_hours") && !sharedPreferences.getBoolean("enable_hours", false)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();

            if (Integer.parseInt(sharedPreferences.getString("default_interval", "60")) >= 3600) {
                editor.putString("default_interval", "60");
                editor.apply();
            }
            if (Integer.parseInt(sharedPreferences.getString("maximal_interval", "600")) >= 3600) {
                editor.putString("maximal_interval", "600");
                editor.apply();
            }
        }
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        int newInterval;

        if (preference instanceof EditTextPreference) {

            try {
                newInterval = Integer.parseInt((String) newValue);
            } catch (NumberFormatException nfe) {
                Toast.makeText(getContext(), "Please enter an integer number", Toast.LENGTH_LONG).show();
                return false;
            }

            if ((!sharedPreferences.getBoolean("enable_hours", false)) && newInterval >= 3600) {
                Toast.makeText(getContext(), "Please enter an integer lesser 3600 (1h)", Toast.LENGTH_LONG).show();
                return false;
            } else if (sharedPreferences.getBoolean("enable_hours", false) && newInterval > 86400) {
                Toast.makeText(getContext(), "Please enter an integer lesser or equal 86400 (24h)", Toast.LENGTH_LONG).show();
                return false;
            }

            if (preference.getKey().equals("default_interval")) {
                if (newInterval > Integer.parseInt(sharedPreferences.getString("maximal_interval", "600"))) {
                    Toast.makeText(getContext(), "Default interval should be <= maximal interval", Toast.LENGTH_LONG).show();
                    return false;
                }
            } else if (preference.getKey().equals("maximal_interval")) {
                if (newInterval < Integer.parseInt(sharedPreferences.getString("default_interval", "60"))) {
                    Toast.makeText(getContext(), "Maximal interval should be >= default interval ", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        }

        return true;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
