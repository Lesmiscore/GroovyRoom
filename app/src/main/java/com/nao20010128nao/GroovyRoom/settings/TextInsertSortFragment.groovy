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

package com.nao20010128nao.GroovyRoom.settings

import android.os.Bundle
import android.support.annotation.Nullable
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nao20010128nao.GroovyRoom.R
import com.nao20010128nao.GroovyRoom.VH
import groovy.transform.CompileStatic

class TextInsertSortFragment extends Fragment {
    List<String> strings

    @Override
    void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)

    }

    @Override
    void onResume() {
        super.onResume()
        def settings=new ConfigManager(context)
        strings=settings.textInsertStrings

        def changeCallback={->
            settings.textInsertStrings = strings
        }

        def rv=(RecyclerView)view.findViewById(R.id.list)
        rv.layoutManager=new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
        rv.adapter=new InternalAdapter()
        rv.adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            void onChanged() {
                changeCallback()
            }

            @Override
            void onItemRangeChanged(int positionStart, int itemCount) {
                changeCallback()
            }

            @Override
            void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                changeCallback()
            }

            @Override
            void onItemRangeInserted(int positionStart, int itemCount) {
                changeCallback()
            }

            @Override
            void onItemRangeRemoved(int positionStart, int itemCount) {
                changeCallback()
            }

            @Override
            void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                changeCallback()
            }
        })
        def ddManager = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                final int fromPos = viewHolder.adapterPosition
                final int toPos = target.adapterPosition
                rv.adapter.notifyItemMoved(fromPos, toPos)
                strings.add(toPos, strings.remove(fromPos))
                changeCallback()
                return true
            }

            @Override
            void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {}

            @Override
            boolean isItemViewSwipeEnabled() {
                return false
            }
            @Override
            boolean isLongPressDragEnabled() {
                return true
            }
        }
        def itemTouch=new ItemTouchHelper(ddManager)
        itemTouch.attachToRecyclerView(rv)
    }

    @Override
    View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        inflater.inflate(R.layout.fragment_text_insert_sort,container,false)
    }

    class InternalAdapter extends RecyclerView.Adapter<VH>{

        @Override
        VH onCreateViewHolder(ViewGroup parent, int viewType) {
            new VH(LayoutInflater.from(context).inflate(R.layout.view_text_insert_draggable_settings,parent,false))
        }

        @Override
        void onBindViewHolder(VH holder, int position) {
            holder[R.id.text]=strings[position]
            /*
            holder[R.id.up]{
                strings.add(position-1,strings.remove(position))
                notifyItemRangeChanged(position-1,position)
            }as View.OnClickListener
            holder[R.id.down]{
                strings.add(position+1,strings.remove(position))
                notifyItemRangeChanged(position,position+1)
            }as View.OnClickListener
            */
        }

        @Override
        int getItemCount() {
            return strings.size()
        }
    }
}
