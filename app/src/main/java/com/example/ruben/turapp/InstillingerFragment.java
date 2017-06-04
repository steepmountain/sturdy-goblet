package com.example.ruben.turapp;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

/**
 * Fragment for håndtering av instillinger
 */
public class InstillingerFragment extends PreferenceFragmentCompat {


    private EditTextPreference etpNavnPref;
    private EditTextPreference etpZoomPref;
    private SharedPreferences mSettings;

    public InstillingerFragment() {
        // Required empty public constructor
    }

    /**
     * Nødvendig override-metode for PreferenceFragmentCompat
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

         // Henter preferanselayout fra res/xml/preferences.xml
        addPreferencesFromResource(R.xml.preferences);

        // SharedPreference-objekt for å skrive settings til
        mSettings = getActivity().getPreferences(Context.MODE_PRIVATE);

        // EditTextPreference for "brukernavn"
        etpNavnPref = (EditTextPreference) findPreference("navn");
        etpNavnPref.setSummary(mSettings.getString("Navn", null));
        etpNavnPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                // Oppdaterer SharedPreference når preference forandrer seg
                String nyNavn = (String) o;
                SharedPreferences.Editor edit = mSettings.edit();
                edit.putString("Navn", nyNavn);
                edit.apply();
                etpNavnPref.setSummary(nyNavn);
                return false;
            }
        });


        // EditTextPreference for zoomfaktor på Google Map i DetaljertFragment
        etpZoomPref = (EditTextPreference) findPreference("zoomFaktor");
        etpZoomPref.setSummary(mSettings.getInt("ZoomFaktor", 10) + "");
        etpZoomPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                // Oppdaterer SharedPreference når preference forandrer seg
                int nyZoom = Integer.parseInt((String)o);
                SharedPreferences.Editor edit = mSettings.edit();
                edit.putInt("ZoomFaktor", nyZoom);
                edit.apply();
                etpZoomPref.setSummary(nyZoom + "");
                return false;
            }
        });
    }

}
