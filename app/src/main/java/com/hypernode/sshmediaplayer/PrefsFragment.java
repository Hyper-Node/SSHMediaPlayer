package com.hypernode.sshmediaplayer;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.hypernode.sshmediaplayer.R;

public class PrefsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

}