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

package com.nao20010128nao.GroovyRoom;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import groovy.transform.CompileStatic;

import static com.nao20010128nao.GroovyRoom.Constants.*

class SaveActivity extends AppCompatActivity {
    RecyclerView rv
    EditText saveName
    Button saveButton

    SavesManager manager

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        setContentView R.layout.activity_save
        manager=new SavesManager(this)
        rv=(RecyclerView)findViewById(R.id.saves)
        saveName=(EditText)findViewById(R.id.saveName)
        saveButton=(Button)findViewById(R.id.save)

        rv.adapter=new SaveList()
        rv.layoutManager=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        saveButton.onClickListener={
            if(!choosingName)return
            manager[choosingName]=intent.getStringExtra(EXTRA_CODE)
            finish()
        }
        saveName.onEditorActionListener={ TextView v, int actionId, KeyEvent event->
            rv.adapter.notifyDataSetChanged()
        }
    }

    String getChoosingName() {
        "$saveName.text"
    }

    void setChoosingName(String choosingName) {
        saveName.text = choosingName
    }


    class SaveList extends RecyclerView.Adapter<VH>{
        @Override
        VH onCreateViewHolder(ViewGroup parent, int viewType) {
            new VH(layoutInflater.inflate(R.layout.saves_name_plate,parent,false))
        }

        @Override
        void onBindViewHolder(VH holder, int position) {
            def name=manager.savedNames[position]
            holder[R.id.savedName]=name
            holder[R.id.selected]=(name==choosingName)?VH.Visibility.VISIBLE:VH.Visibility.GONE
            holder[-1]({
                choosingName=name
                notifyDataSetChanged()
            }as View.OnClickListener)
        }

        @Override
        int getItemCount() {
            manager.savedNames.size()
        }
    }
}
