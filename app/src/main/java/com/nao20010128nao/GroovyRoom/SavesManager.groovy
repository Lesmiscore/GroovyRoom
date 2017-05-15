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
