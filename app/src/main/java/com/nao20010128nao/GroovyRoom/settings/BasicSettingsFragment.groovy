/*
 *    Copyright 2017 nao20010128nao
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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
