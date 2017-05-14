package com.nao20010128nao.GroovyRoom.settings

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import com.nao20010128nao.GroovyRoom.R
import groovy.transform.CompileStatic

/**
 * Created by nao on 2017/05/11.
 */
@CompileStatic
class BasicSettingsFragment extends PreferenceFragmentCompat{
    @Override
    void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.basic_settings)
    }
}
