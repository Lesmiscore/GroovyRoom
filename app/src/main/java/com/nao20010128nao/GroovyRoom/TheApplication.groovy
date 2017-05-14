package com.nao20010128nao.GroovyRoom

import android.app.Application

/**
 * Created by nao on 2017/05/14.
 */
class TheApplication extends Application {
    public static TheApplication instance=null

    @Override
    void onCreate() {
        super.onCreate()
        instance=this
    }
}
