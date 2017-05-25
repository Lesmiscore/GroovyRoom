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

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.nao20010128nao.GroovyRoom.view.TextInsertView
import groovy.transform.CompileStatic
import groovy.transform.Memoized

import static com.nao20010128nao.GroovyRoom.Constants.*
/**
 * R/W class for default SharedPreferences
 */
@CompileStatic
class ConfigManager {
    private Context context
    private SharedPreferences pref

    ConfigManager(Context c){
        assert c
        context=c
        pref=PreferenceManager.getDefaultSharedPreferences(c)
    }

    List<String> getTextInsertStrings(){
        if(pref.contains(PREF_TEXT_INSERT_STRINGS)){
            return gson.fromJson(pref.getString(PREF_TEXT_INSERT_STRINGS,''),ArrayList)
        }
        return new ArrayList<String>(TextInsertView.STANDARD_TEXTS)
    }

    void setTextInsertStrings(List<String> l){
        edit().putString(PREF_TEXT_INSERT_STRINGS,gson.toJson(l)).commit()
    }

    boolean getExposeActivityForNormalRun(){pref.getBoolean(PREF_EXPOSE_ACTIVITY,true)&&pref.getBoolean(PREF_EXPOSE_ACTIVITY_NORMAL,true)}
    boolean getExposeActivityForConfigSlurper(){pref.getBoolean(PREF_EXPOSE_ACTIVITY,true)&&pref.getBoolean(PREF_EXPOSE_ACTIVITY_CONFIGSLURPER,true)}

    int getConnectionPort(){pref.getInt(PREF_CONNECTION_PORT,52789/*Random value from keyboard*/)}
    void setConnectionPort(int value){edit().putInt(PREF_CONNECTION_PORT,value).commit()}

    private SharedPreferences.Editor edit(){pref.edit()}
    @Memoized private Gson getGson(){new Gson()}
}
