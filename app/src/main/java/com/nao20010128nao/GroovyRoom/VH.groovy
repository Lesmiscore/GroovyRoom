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