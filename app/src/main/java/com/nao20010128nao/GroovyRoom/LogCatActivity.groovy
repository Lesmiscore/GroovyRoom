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

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView
import groovy.transform.CompileStatic

class LogCatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        setContentView R.layout.activity_log_cat
        ((TextView)findViewById(R.id.logcat)).with{
            text=''
            new Thread({
                ['logcat'].execute().in.eachLine {line->
                    runOnUiThread{
                        append "$line\n"
                    }
                }
                runOnUiThread{
                    ((ScrollView)parent).with{
                        fullScroll(FOCUS_DOWN)
                    }
                }
            }).start()
        }
    }
}
