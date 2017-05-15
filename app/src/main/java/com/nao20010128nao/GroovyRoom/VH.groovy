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

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import groovy.transform.CompileStatic

/**
 * Created by nao on 2017/05/07.
 */
@CompileStatic
class VH extends RecyclerView.ViewHolder{
    VH(View itemView) {
        super(itemView)
    }
    void putAt(int id,String text){
        def view=findViewById(id)
        if(view instanceof TextView){
            view.text=text
        }
    }
    void putAt(int id,boolean check){
        def view=findViewById(id)
        if(view instanceof CompoundButton){
            view.checked=check
        }
    }
    void putAt(int id,Visibility visibility){
        def view=findViewById(id)
        if(view){
            view.visibility=visibility.value
        }
    }
    Closure getAt(int id){
        if(id==-1){
            return {val->
                if(val instanceof View.OnClickListener){
                    Utils.applyHandlersForViewTree(itemView,val)
                }
                if(val instanceof View.OnLongClickListener){
                    Utils.applyHandlersForViewTree(itemView,val)
                }
            }
        }else{
            def view=findViewById(id)
            assert view
            return {val->
                if(val instanceof View.OnClickListener){
                    view.onClickListener=val
                }
                if(val instanceof View.OnLongClickListener){
                    view.onLongClickListener=val
                }
            }
        }
    }
    View findViewById(int id){itemView.findViewById(id)}

    static enum Visibility {
        VISIBLE(View.VISIBLE),INVISIBLE(View.INVISIBLE),GONE(View.GONE)

        final int value
        private Visibility(int v){value=v}
    }
}