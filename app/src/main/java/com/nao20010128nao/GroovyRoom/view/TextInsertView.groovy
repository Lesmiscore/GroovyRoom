package com.nao20010128nao.GroovyRoom.view

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.nao20010128nao.GroovyRoom.R
import com.nao20010128nao.GroovyRoom.VH

/**
 * Created by nao on 2017/05/09.
 */
class TextInsertView extends FrameLayout{
    public static List<String> STANDARD_TEXTS=Collections.unmodifiableList(['  ',',','{','}','(',')','[',']','<','>','{}','()','[]','<>','def ','->','\'','"','\\','+','-','*','/','if()','if(){}'])

    private boolean inited=false
    private List<String> strings=[]
    private RecyclerView textList
    private TextView linkedView

    TextInsertView(Context context) {
        super(context)
        init()
    }

    TextInsertView(Context context, AttributeSet attrs) {
        super(context, attrs)
        init()
    }

    TextInsertView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr)
        init()
    }

    TextInsertView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes)
        init()
    }

    private void init(){
        if(inited){
            return
        }
        LayoutInflater.from(context).inflate(R.layout.view_text_insert,this)
        textList=(RecyclerView)findViewById(R.id.texts)
        textList.layoutManager=new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        textList.adapter=new InternalAdapter()
        inited=true
    }

    void setList(List<String> e){
        strings=e
        textList.adapter.notifyDataSetChanged()
    }

    List<String> getList(){strings}

    void initStandardText(){
        list=STANDARD_TEXTS
    }

    void linkTextView(TextView tv){
        linkedView=tv
    }

    class InternalAdapter extends RecyclerView.Adapter<VH>{

        @Override
        VH onCreateViewHolder(ViewGroup parent, int viewType) {
            new VH(LayoutInflater.from(context).inflate(R.layout.view_text_insert_label,parent,false))
        }

        @Override
        void onBindViewHolder(VH holder, int position) {
            holder[R.id.text]=strings[position]
            holder[R.id.text]({
                def start=linkedView.selectionStart
                def end=linkedView.selectionEnd
                linkedView.editableText.replace(start,end,strings[position])
            } as View.OnClickListener)
        }

        @Override
        int getItemCount() {
            return strings.size()
        }
    }
}
