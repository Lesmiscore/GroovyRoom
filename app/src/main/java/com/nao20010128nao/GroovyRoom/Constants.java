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

/**
 * Created by nao on 2017/05/07.
 */
public class Constants {
    public static final String ACTION_NEW="create_new";
    public static final String ACTION_LOAD="load";
    public static final String EXTRA_CODE_SAVE="code_save_name";
    public static final String EXTRA_CODE="code";
    public static final String EXTRA_MODE="mode";
    public static final String EXTRA_FRAGMENT_CLASS="fragment_class";
    public static final ExecutionMode EXECUTION_MODE_NORMALLY=ExecutionMode.NORMALLY;
    public static final ExecutionMode EXECUTION_MODE_CONFIGSLURPER=ExecutionMode.CONFIGSLURPER;
    public static final ExecutionMode EXECUTION_MODE_COMPILE_ONLY=ExecutionMode.COMPILE_ONLY;
    public static final String PREF_TEXT_INSERT_STRINGS="textInsertStrings";
    public static final String PREF_EXPOSE_ACTIVITY="exposeActivity";
    public static final String PREF_EXPOSE_ACTIVITY_NORMAL="exposeActivity.normal";
    public static final String PREF_EXPOSE_ACTIVITY_CONFIGSLURPER="exposeActivity.configslurper";
    public static final String PREF_CONNECTION_PORT="connectionPort";

    public static final ThreadGroup THREAD_GROUP_EXECUTION_WORKER=new ThreadGroup("executionWorker");


    public enum ExecutionMode{
        NORMALLY(ExecutionBehavior.DIRECT,true,false,"normal"),
        CONFIGSLURPER(ExecutionBehavior.CONFIGSLURPER,false,true,"configslurper"),
        COMPILE_ONLY(ExecutionBehavior.NULL,false,false,"compileonly");

        public final ExecutionBehavior behavior;
        public final boolean handleOutput,configSlurper;
        private final String inStr;
        ExecutionMode(ExecutionBehavior behavior,boolean handleOutput,boolean configSlurper,String inStr){
            this.behavior=behavior;
            this.handleOutput=handleOutput;
            this.configSlurper=configSlurper;
            this.inStr=inStr;
        }

        @Override
        public String toString() {
            return inStr;
        }

        public enum ExecutionBehavior{
            DIRECT,CONFIGSLURPER,NULL;
        }
    }
}
