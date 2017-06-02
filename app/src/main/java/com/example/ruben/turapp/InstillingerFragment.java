package com.example.ruben.turapp;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class InstillingerFragment extends PreferenceFragmentCompat {


    private EditTextPreference etpNavnPref;
    private EditTextPreference etpZoomPref;
    private SharedPreferences mSettings;

    public InstillingerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        mSettings = getActivity().getPreferences(Context.MODE_PRIVATE);

        etpNavnPref = (EditTextPreference) findPreference("navn");
        etpNavnPref.setSummary(mSettings.getString("Navn", null));
        etpNavnPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                String nyNavn = (String) o;
                SharedPreferences.Editor edit = mSettings.edit();
                edit.putString("Navn", nyNavn);
                edit.apply();
                etpNavnPref.setSummary(nyNavn);
                return false;
            }
        });


        etpZoomPref = (EditTextPreference) findPreference("zoomFaktor");
        etpZoomPref.setSummary(mSettings.getInt("ZoomFaktor", 10) + "");
        etpZoomPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
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
