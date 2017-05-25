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

package com.nao20010128nao.GroovyRoom.connection

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.nao20010128nao.GroovyRoom.ExecutionActivity
import com.nao20010128nao.GroovyRoom.SavesManager
import com.nao20010128nao.GroovyRoom.settings.ConfigManager
import groovy.transform.CompileStatic

import java.lang.ref.WeakReference

import static com.nao20010128nao.GroovyRoom.Constants.*

class ConnectionService extends Service {
    public static WeakReference<ConnectionService> instance=new WeakReference<>(null)

    SockServer server
    SavesManager saves
    boolean running=false

    ConnectionService() {
    }

    @Override
    IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented")
    }

    @Override
    int onStartCommand(Intent intent, int flags, int startId) {
        instance=new WeakReference<>(this)
        def cm=new ConfigManager(this)
        saves=new SavesManager(this)
        (server=new SockServer(cm.connectionPort)).start(5000*2)
        running=true
        return START_NOT_STICKY
    }

    class SockServer {
        private final List<Class<? extends SockServerServe>> parts=[
                ServeExecute,ServeSave,ServeLoad,ServeList,ServePing
        ]
        private final Map<String,SockServerServe> partsMap

        private Thread t=null
        private final int port

        SockServer(int port) {
            this.port=port
            def pm=[:]
            parts.each {
                def ins=it.newInstance()
                pm[ins.dir]=ins
            }
            partsMap=pm
        }

        void start(int timeout=5000){
            assert !t
            (t=new Thread({
                Thread localThread=t
                def ss=null
                try {
                    ss = new ServerSocket(port)
                    while(!localThread.interrupted){
                        try{
                            final sock=ss.accept()
                            try {
                                ss.soTimeout=timeout
                                def objIs=sock.inputStream.newObjectInputStream()
                                def dir=objIs.readUTF()
                                Log.d('SockServer',"Passing $dir command to ${partsMap[dir]}")
                                def got=partsMap[dir](sock,objIs,sock.outputStream)
                                if(got){
                                    sock.outputStream.write(got)
                                }
                            }finally {
                                sock.close()
                            }
                        }catch(Throwable e){
                            Log.e('SockServer','Error',e)
                        }
                    }
                }finally {
                    ss?.close()
                }
            })).start()
        }

        void stop(){
            t.interrupt()
            t=null
        }
    }

    interface SockServerServe {
        String getDir()
        byte[] call(Socket session,InputStream is,OutputStream os)
    }

    class ServeExecute implements SockServerServe{
        @Override @CompileStatic
        String getDir() {
            'execute'
        }

        @Override
        byte[] call(Socket session,InputStream is,OutputStream os) {
            Log.d('Execute','Requested execution')
            def flag=Structures.Execute.parseFrom(is)
            def intent=new Intent(ConnectionService.this,ExecutionActivity)
            intent.flags|=Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(EXTRA_MODE,{
                def internalMode=flag.mode
                switch(internalMode){
                    case Structures.Execute.Mode.NORMAL:
                        return EXECUTION_MODE_NORMALLY
                    case Structures.Execute.Mode.CONFIGSLURPER:
                        return EXECUTION_MODE_CONFIGSLURPER
                    case Structures.Execute.Mode.COMPILE_ONLY:
                        return EXECUTION_MODE_COMPILE_ONLY
                    // no otherwise: switching by non-null enum
                }
            }())
            intent.putExtra(EXTRA_CODE,flag.code)
            startActivity(intent)
            'OK'.bytes
        }
    }

    class ServeSave implements SockServerServe{
        @Override @CompileStatic
        String getDir() {
            'save'
        }

        @Override
        byte[] call(Socket session,InputStream is,OutputStream os) {
            Log.d('Save','Requested saving')
            def flag=Structures.Save.parseFrom(session.inputStream)
            def result=saves.save(flag.name,flag.code)
            Structures.Save.Reply
                    .newBuilder()
                    .setResult(result?Structures.Save.Reply.SaveResult.OK:Structures.Save.Reply.SaveResult.NG)
                    .build().toByteArray()
        }
    }
    class ServeLoad implements SockServerServe{
        @Override @CompileStatic
        String getDir() {
            'load'
        }

        @Override
        byte[] call(Socket session,InputStream is,OutputStream os) {
            Log.d('Load','Requested loading')
            def flag=Structures.Load.parseFrom(session.inputStream)
            def result=saves[flag.name]
            Structures.Load.Reply
                    .newBuilder()
                    .setCode(result).setFound(!!result)
                    .build().toByteArray()
        }
    }
    class ServeList implements SockServerServe{
        @Override @CompileStatic
        String getDir() {
            'list'
        }

        @Override
        byte[] call(Socket session,InputStream is,OutputStream os) {
            Log.d('List','Requested listing up')
            Structures.List
                    .newBuilder()
                    .addAllName(saves.savedNames.sort())
                    .build().toByteArray()
        }
    }
    class ServePing implements SockServerServe{
        @Override @CompileStatic
        String getDir() {
            'ping'
        }

        @Override
        byte[] call(Socket session,InputStream is,OutputStream os) {
            Log.d('Ping','Requested to pong')
            def flag=Structures.Ping.parseFrom(session.inputStream)
            Structures.Ping.Pong
                    .newBuilder()
                    .setAnyString(flag.anyString.reverse())
                    .build().toByteArray()
        }
    }
}
