package com.nao20010128nao.GroovyRoom

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import groovy.transform.CompileStatic

/**
 * Created by nao on 2017/05/07.
 */
@CompileStatic
class SavesManager {
    private final Context context
    private final File dir
    SavesManager(Context c){
        context=c
        dir=new File(c.filesDir,'codeSaves')
        assert dir.exists()||dir.mkdirs()&&dir.exists()
        /*c.getSharedPreferences('code_saves',0).all.each {kv->
            save(kv.key,"$kv.value")
        }*/
    }

    List<String> getSavedNames(){
        dir.listFiles()*.name.sort()
    }

    boolean save(String name,String code){
        Log.d('SavesManager',"Saving $name")
        try {
            new File(dir,name).text=code
        }catch (Throwable e){
            !e// must be false
        }
    }

    String load(String name){
        Log.d('SavesManager',"Loading $name")
        new File(dir,name).text
    }

    void putAt(String name,String code){
        save(name,code)
    }

    String getAt(String name){
        load(name)
    }

    boolean remove(String key){
        new File(dir,key).delete()
    }

    boolean rename(String f,String d){
        new File(dir,f).renameTo(new File(dir,d))
    }
}
