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

import android.view.View
import android.view.ViewGroup

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
    static byte[] readBytes(InputStream is){
        def objStrm=new BufferedInputStream(is).newObjectInputStream()
        def buf=new byte[objStrm.readInt()]
        objStrm.readFully(buf)
        return buf
    }
}
