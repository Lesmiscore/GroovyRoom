package com.nao20010128nao.GroovyRoom

import android.view.View
import android.view.ViewGroup
import groovy.transform.CompileStatic

/**
 * Created by nao on 2017/05/07.
 */
class Utils {
    static void applyHandlersForViewTree(View v, View.OnClickListener click){
        if(v){
            v.onClickListener=click
            if(v instanceof ViewGroup){
                for(int i=0;i<v.childCount;i++){
                    applyHandlersForViewTree(v.getChildAt(i),click)
                }
            }
        }
    }
    static void applyHandlersForViewTree(View v,View.OnLongClickListener longer){
        if(v){
            v.onLongClickListener=longer
            v.longClickable=!!longer
            if(v instanceof ViewGroup){
                for(int i=0;i<v.childCount;i++){
                    applyHandlersForViewTree(v.getChildAt(i),longer)
                }
            }
        }
    }
}
