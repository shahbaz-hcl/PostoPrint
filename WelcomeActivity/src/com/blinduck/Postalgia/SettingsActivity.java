package com.blinduck.Postalgia;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockPreferenceActivity;

/**
 * Created with IntelliJ IDEA.
 * User: deepan
 * Date: 7/4/13
 * Time: 12:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class SettingsActivity extends SherlockPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private EditTextPreference firstName;
    private EditTextPreference lastName;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        sharedPreferences = getPreferenceScreen().getSharedPreferences();

        firstName = (EditTextPreference) getPreferenceScreen().findPreference("firstName");
        lastName = (EditTextPreference) getPreferenceScreen().findPreference("lastName");



    }

    @Override
    protected void onResume() {
        super.onResume();
        firstName.setSummary(sharedPreferences.getString("firstName", ""));
        lastName.setSummary(sharedPreferences.getString("lastName", ""));
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("firstName")) firstName.setSummary(sharedPreferences.getString(key, ""));
        else if (key.equals("lastName")) lastName.setSummary(sharedPreferences.getString(key, ""));

    }



}
