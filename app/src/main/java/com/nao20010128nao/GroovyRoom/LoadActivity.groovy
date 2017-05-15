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

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import groovy.transform.CompileStatic

import static com.nao20010128nao.GroovyRoom.Constants.*
import static com.nao20010128nao.GroovyRoom.R.*

class LoadActivity extends AppCompatActivity {
    RecyclerView rv
    Button loadButton
    String choosingName

    SavesManager manager

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        setContentView R.layout.activity_load
        manager=new SavesManager(this)
        rv=(RecyclerView)findViewById(R.id.saves)
        loadButton=(Button)findViewById(R.id.open)

        rv.adapter=new SaveList()
        rv.layoutManager=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        loadButton.onClickListener={
            if(!choosingName)return
            startActivity new Intent(this,CodeActivity).setAction(ACTION_LOAD).putExtra(EXTRA_CODE_SAVE,"$choosingName" as String)
            finish()
        }
        loadButton.enabled=false
    }

    class SaveList extends RecyclerView.Adapter<VH>{
        @Override
        VH onCreateViewHolder(ViewGroup parent, int viewType) {
            new VH(layoutInflater.inflate(R.layout.saves_name_plate,parent,false))
        }

        @Override
        void onBindViewHolder(VH holder, int position) {
            def ctx=LoadActivity.this

            def name=manager.savedNames[position]
            holder[R.id.savedName]=name
            holder[R.id.selected]=(name==choosingName)?VH.Visibility.VISIBLE:VH.Visibility.GONE
            holder[-1]({
                choosingName=name
                notifyDataSetChanged()
                loadButton.enabled=true
            }as View.OnClickListener)
            holder[-1]({
                def rename={->
                    new AlertDialog.Builder(ctx).with {
                        title=resources.getString(R.string.rename_to,name)
                        def local=view=layoutInflater.inflate(R.layout.dialog_rename,new FrameLayout(ctx),false)
                        def editText=(EditText)local.findViewById(R.id.renameTo)
                        editText.text=name
                        setPositiveButton(android.R.string.yes){di,w->
                            manager.rename(name,"$editText.text")
                            notifyDataSetChanged()
                        }
                        setNegativeButton(android.R.string.no){di,w->

                        }
                        show()
                    }
                }
                def delete={->
                    new AlertDialog.Builder(ctx).with {
                        title=resources.getString(R.string.delete_file,name)
                        message=R.string.are_you_sure
                        setNegativeButton(android.R.string.yes){di,w->
                            manager.remove(name)
                            notifyItemRemoved(position)
                        }
                        setPositiveButton(android.R.string.no){di,w->

                        }
                        show()
                    }
                }
                new AlertDialog.Builder(ctx).with {
                    title=name
                    setItems(resources.getIdentifier('saveEditName','array',packageName)){ di, w->
                        switch (w){
                            case 0://rename
                                rename()
                                break
                            case 1://delete
                                delete()
                                break
                        }
                    }
                    show()
                }
                return true
            }as View.OnLongClickListener)
        }

        @Override
        int getItemCount() {
            manager.savedNames.size()
        }
    }
}
