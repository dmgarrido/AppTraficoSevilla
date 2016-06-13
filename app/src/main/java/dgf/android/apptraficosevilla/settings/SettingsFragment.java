package dgf.android.apptraficosevilla.settings;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import dgf.android.apptraficosevilla.R;


public class SettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_markers_key)));
        bindCheckLocationPreference(findPreference(getString(R.string.pref_checkLocation_key)));
    }

    private void bindCheckLocationPreference(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);
    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.

        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        Log.v("Trafico", "onPreferenceChange" );
        if (preference instanceof SeekBarPreference) {
            String texto = ((SeekBarPreference) preference).getTextProgress();
            Log.v("Trafico", "Seekbar preference " + texto);
        }

        if (preference instanceof CheckBoxPreference) {
            //((CheckBoxPreference) preference).setChecked((boolean)value);
            Log.v("Trafico", "preference checkbox" );
        }
        else {
            String stringValue = value.toString();
            Log.v("Trafico", "preference " + stringValue);
            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list (since they have separate labels/values).
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(stringValue);
                if (prefIndex >= 0) {
                    preference.setSummary(listPreference.getEntries()[prefIndex]);
                }
            } else {
                // For other preferences, set the summary to the value's simple string representation.
                preference.setSummary(stringValue);
            }

        }
        return true;
    }

}
