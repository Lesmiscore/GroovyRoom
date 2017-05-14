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
