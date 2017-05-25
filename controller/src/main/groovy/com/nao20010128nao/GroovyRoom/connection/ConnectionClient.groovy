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

import joptsimple.OptionParser
import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

def ob=new OptionParser()
ob.accepts('ip').withRequiredArg()
ob.accepts('port').withOptionalArg()
def parsed=ob.parse(args)

def ip="${parsed.has('ip')?parsed.valueOf('ip'):null}"
def port
try {
    port="${parsed.has('port')?parsed.valueOf('port'):'-1'}".toInteger()
}catch (_){
    port=-1
}

def httpClient=HttpClientBuilder.create().build()

def easyPost={String addr,byte[] send->
    def post=new HttpPost(addr)
    post.entity=new ByteArrayEntity(send)
    post.setHeader("Content-Type",'application/octet-stream')
    def response=null
    try {
        response = httpClient.execute(post)
        if (response.statusLine.statusCode == HttpStatus.SC_OK) {
            return EntityUtils.toByteArray(response.entity)
        }else if(response.statusLine.statusCode >=400){
            throw new IOException("HTTP Error: $response.statusLine.statusCode")
        }
    }finally{
        response?.close()
    }
}

def upload={String addr,byte[] send=null->
    def url=new URL(addr)
    def host=url.host
    def prt=url.port
    def dir=url.path.substring(1)/*cut a slash*/
    def sock=null
    try {
        sock=new Socket(host,prt)
        def os=sock.outputStream.newObjectOutputStream()
        os.writeUTF(dir)
        if(send)os.write(send)
        os.flush()
        return sock.inputStream.bytes
    }finally {
        sock?.close()
    }
}

def easyGet={String addr->
    def post=new HttpGet(addr)
    def response=null
    try {
        response = httpClient.execute(post)
        if (response.statusLine.statusCode == HttpStatus.SC_OK) {
            return EntityUtils.toByteArray(response.entity)
        }else if(response.statusLine.statusCode >=400){
            throw new IOException("HTTP Error: $response.statusLine.statusCode")
        }
    }finally{
        response?.close()
    }
}

def tryPing={String aIp=ip,int aPort=port->
    def ping=Structures.Ping.newBuilder().setAnyString('Yeah').build().toByteArray()
    def pong=Structures.Ping.Pong.parseFrom(upload("http://$aIp:$aPort/ping",ping))
    return pong
}


final mainDialog={->
    final executionDialog={code,Structures.Execute.Mode mode,ActionEvent _=null->
        //def window=new JFrame()
        // TODO: run function
        // TODO: implement server side on Android app
        // Only send protobuf to the server and run the code alone
        def strCode
        if(code instanceof Closure){
            strCode="${code()}"
        }else{
            strCode="$code"
        }
        def toSend=Structures.Execute.newBuilder()
                .setCode(strCode)
                .setMode(mode)
                .build().toByteArray()
        upload("http://$ip:$port/execute",toSend)
    }


    final saveDialog={code,ActionEvent _=null->
        final String strCode
        if(code instanceof Closure){
            strCode="${code()}"
        }else{
            strCode="$code"
        }

        def window=new JFrame()
        def listView=new JList()
        def saveName=new JTextField()
        def saveButton=new JButton('Save')

        def files=Structures.List.parseFrom(easyGet("http://$ip:$port/list")).nameList.toArray()
        listView.listData = files
        listView.selectionMode=ListSelectionModel.SINGLE_SELECTION
        listView.addListSelectionListener{e->
            saveName.text=files[e.firstIndex].toString()
        }

        saveButton.addActionListener{
            def toSend=Structures.Save.newBuilder()
                    .setName(saveName.text)
                    .setCode(strCode)
                    .build().toByteArray()
            upload("http://$ip:$port/save",toSend)
            window.visible=false
        }

        def bottomFlow=new JPanel()
        bottomFlow.layout=new BoxLayout(bottomFlow,BoxLayout.X_AXIS)
        [saveName,saveButton].each {bottomFlow.add(it)}

        window.contentPane.layout=new BoxLayout(window.contentPane,BoxLayout.Y_AXIS)
        [listView,bottomFlow].each {window.add(it)}

        window.visible=true
    }

    final codeDialog={ String name=null, String code=null, ActionEvent _=null->
        def window=new JFrame()
        window.title="Code${name?" - $name":''}"

        def codeZone=new JTextArea(code?:'')
        codeZone.font = new Font("monospaced", Font.PLAIN, 12)
        window.add(codeZone)

        (window.JMenuBar=new JMenuBar()).with {
            def save=new JMenu('Save')
            add(save)
            def run=new JMenu('Run')
            add(run)

            def runNormally=new JMenuItem('Run normally')
            run.add(runNormally)
            def runConfigSlurper=new JMenuItem('Run as ConfigSlurper')
            run.add(runConfigSlurper)

            def save2=new JMenuItem('Save')
            save.add(save2)

            runNormally.addActionListener executionDialog.curry({codeZone.text},Structures.Execute.Mode.NORMAL)
            runConfigSlurper.addActionListener executionDialog.curry({codeZone.text},Structures.Execute.Mode.CONFIGSLURPER)
            save2.addActionListener saveDialog.curry({codeZone.text})
        }

        window.visible=true
    }

    final loadDialog={_->
        def window=new JFrame()
        def listView=new JList()
        def loadButton=new JButton('List')

        def lastSelection=''

        def files=Structures.List.parseFrom(upload("http://$ip:$port/list")).nameList.toArray()
        listView.listData = files
        listView.selectionMode=ListSelectionModel.SINGLE_SELECTION
        listView.addListSelectionListener{e->
            lastSelection=files[e.firstIndex].toString()
        }

        loadButton.addActionListener{
            def toSend=Structures.Load.newBuilder()
                    .setName(lastSelection)
                    .build().toByteArray()
            def code=Structures.Load.Reply
                    .parseFrom(upload("http://$ip:$port/load",toSend))
                    .getCode()
            window.visible=false
            codeDialog(lastSelection,code)
        }

        def bottomFlow=new JPanel()
        bottomFlow.layout=new BoxLayout(bottomFlow,BoxLayout.X_AXIS)
        bottomFlow.add(loadButton)

        window.contentPane.layout=new BoxLayout(window.contentPane,BoxLayout.Y_AXIS)
        [listView,bottomFlow].each {window.add(it)}

        window.visible=true
    }

    def window=new JFrame()
    window.title="GroovyRoom - $ip:$port"
    window.defaultCloseOperation=JFrame.EXIT_ON_CLOSE

    window.setSize(320,240)

    window.layout=new GridLayout(4,1)

    def newButton=new JButton('New')
    newButton.addActionListener codeDialog.curry(null,null)

    def loadButton=new JButton('Load')
    loadButton.addActionListener loadDialog

    window.add(newButton)
    window.add(loadButton)

    window
}

def serverTypeDialog={String aIp=null,int aPort=-1->
    def preventExit=false

    def window=new JFrame()
    window.title='Connect'

    window.addWindowListener(new WindowAdapter() {
        @Override
        void windowClosing(WindowEvent e) {
            if(!preventExit){
                System.exit 0
            }
        }
    })

    window.setSize(320, 240)
    window.minimumSize = new Dimension(320, 240)

    def pane=new JPanel()
    def paneLayout = new GroupLayout(pane)
    pane.layout=paneLayout

    def vGroup = paneLayout.createSequentialGroup()
    def hGroup = paneLayout.createSequentialGroup()

    def groupLabel = paneLayout.createParallelGroup()
    def groupTField = paneLayout.createParallelGroup()

    def ipGroup = paneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
    def ipLabel=new JLabel('IP:')
    def ipField=new JTextField(aIp?:'localhost')
    [ipLabel,ipField].each {
        ipGroup.addComponent(it)
    }
    vGroup.addGroup(ipGroup)

    def portGroup = paneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
    def portLabel=new JLabel('Port:')
    def portField=new JSpinner(new SpinnerNumberModel(0, 0, 65535, 1))
    portField.value=aPort!=-1?aPort:52789
    [portLabel,portField].each {
        portGroup.addComponent(it)
    }
    vGroup.addGroup(portGroup)

    [ipLabel,portLabel].each {
        groupLabel.addComponent(it)
    }
    [ipField,portField].each {
        groupTField.addComponent(it)
    }

    [groupLabel,groupTField].each {
        hGroup.addGroup(it)
    }

    [ipLabel,portLabel,ipField,portField].each {
        pane.add(it)
    }


    paneLayout.verticalGroup = vGroup
    paneLayout.horizontalGroup = hGroup

    def buttonWrapPane=new JPanel()
    buttonWrapPane.layout=new BorderLayout()

    def connectButton=new JButton()
    connectButton.text='Connect'
    connectButton.addActionListener{e->
        window.enabled=false
        def locIP=ipField.text
        def locPort=(int)portField.value
        new Thread({
            try {
                assert tryPing(locIP,locPort)
                ip=locIP
                port=locPort
                SwingUtilities.invokeLater{
                    preventExit=true
                    window.visible=false
                    mainDialog().visible=true
                }
            }catch (Throwable _){
                SwingUtilities.invokeLater{
                    window.enabled=!!_
                }
            }

        }).start()
    }
    buttonWrapPane.add(connectButton,BorderLayout.SOUTH)


    def grid=new GridLayout(2,1)
    window.layout=grid
    window.add(pane)
    window.add(buttonWrapPane)

    window
}
if(ip&&port!=-1&&tryPing()){
    // go ahead
    mainDialog().visible=true
}else{
   // ask
    serverTypeDialog().visible=true
}
