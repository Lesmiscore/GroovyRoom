package com.nao20010128nao.GroovyRoom

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.nao20010128nao.GroovyRoom.settings.SettingListActivity
import groovy.transform.CompileStatic

import static com.nao20010128nao.GroovyRoom.Constants.*

@CompileStatic
class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView R.layout.activity_main
        findViewById(R.id.newBtn).onClickListener={
            startActivity new Intent(this,CodeActivity).setAction(ACTION_NEW)
        }
        findViewById(R.id.saves).onClickListener={
            startActivity new Intent(this,LoadActivity)
        }
        findViewById(R.id.logcat).onClickListener={
            startActivity new Intent(this,LogCatActivity)
        }
        findViewById(R.id.settings).onClickListener={
            startActivity new Intent(this,SettingListActivity)
        }
    }
}
