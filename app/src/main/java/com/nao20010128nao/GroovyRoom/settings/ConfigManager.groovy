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


    private SharedPreferences.Editor edit(){pref.edit()}
    @Memoized private Gson getGson(){new Gson()}
}
