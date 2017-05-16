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

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.annotation.StyleRes
import android.support.design.widget.BottomSheetDialog
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ContextThemeWrapper
import android.support.v7.widget.Toolbar
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.stream.JsonWriter
import com.nao20010128nao.GroovyRoom.settings.ConfigManager
import me.champeau.groovydroid.GrooidShell
import org.apache.commons.io.output.WriterOutputStream

import static com.nao20010128nao.GroovyRoom.Constants.*

class ExecutionActivity extends AppCompatActivity {
    GrooidShell shell
    TextView console
    def executionFinished=false
    def executionInternal=[:]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        setContentView R.layout.activity_execution
        findViewById(R.id.exitButton).onClickListener={finish()}
        def manager=new SavesManager(this)
        def config=new ConfigManager(this)
        def progressFrame=(FrameLayout)findViewById(R.id.progressFrame)
        def progress=(TextView)findViewById(R.id.progress)
        console=(TextView)findViewById(R.id.console)
        shell=new GrooidShell(cacheDir,classLoader)
        def code=intent.getStringExtra(EXTRA_CODE)
        def mode=intent.getStringExtra(EXTRA_MODE)
        layoutInflater.inflate(R.layout.execution_compiling_prog,progressFrame)
        new Thread(THREAD_GROUP_EXECUTION_WORKER,{
            def swapProgress={@StyleRes int id,@StringRes int str->
                runOnUiThread{
                    progressFrame.removeAllViews()
                    LayoutInflater.from(new ContextThemeWrapper(this,id))
                            .inflate(R.layout.execution_compiling_prog,progressFrame)
                    progress.text=str
                }
            }
            def script
            try{
                def result=shell.evaluate(code){prog->
                    switch(prog){
                        case GrooidShell.EvalProgressCategory.COMPILING:
                            swapProgress(R.style.AppTheme_Color1,R.string.compiling)
                            break
                        case GrooidShell.EvalProgressCategory.DEXING:
                            swapProgress(R.style.AppTheme_Color2,R.string.dexing)
                            break
                    }
                }
                executionInternal.result=result
                script=result.script
                assert script
            }catch(Throwable e){
                runOnUiThread{
                    def dialog=new ErrorDialog()
                    dialog.throwable=e
                    dialog.show()
                }
                return
            }
            swapProgress(R.style.AppTheme_Color3,R.string.wait)
            def outBucket=new StreamBucket()
            outBucket.error=false
            def errBucket=new StreamBucket()
            errBucket.error=true
            def defOut=System.out
            def defErr=System.err
            if(mode==EXECUTION_MODE_NORMALLY){
                if(config.exposeActivityForNormalRun)
                    script.binding.setVariable('context',this)
                script.binding.setVariable('out',outBucket)
                def outPs=new PrintStream(new WriterOutputStream(outBucket))
                def errPs=new PrintStream(new WriterOutputStream(errBucket))
                System.out=outPs
                System.err=errPs
            }
            swapProgress(R.style.AppTheme_Color4,R.string.running)
            try {
                if (mode == EXECUTION_MODE_NORMALLY) {
                    script.run()
                } else if (mode == EXECUTION_MODE_CONFIGSLURPER) {
                    def cs=new ConfigSlurper()
                    if(config.exposeActivityForConfigSlurper){
                        def binding=[:]
                        binding.context=this
                        cs.binding=binding
                    }

                    def output = new StringWriter()
                    def jw = new JsonWriter(output)
                    jw.indent = " " * 4
                    new Gson().toJson(cs.parse(script), ConfigObject, jw)
                    jw.flush()
                    runOnUiThread {
                        console.text = output.toString()
                    }
                }
                executionFinished=true
            }catch(Throwable e){
                runOnUiThread{
                    def dialog=new ErrorDialog()
                    dialog.throwable=e
                    dialog.title=R.string.execution_error
                    dialog.show()
                }
            }finally{
                System.out=defOut
                System.err=defErr
            }
            runOnUiThread{
                findViewById(R.id.progRoot).visibility=View.GONE
                invalidateOptionsMenu()
            }
        },toString(),10*1024*1024/* 10MB for stack size */).start()
    }

    @Override
    void onBackPressed() {}

    @Override
    boolean onCreateOptionsMenu(Menu menu) {
        if(executionFinished&&executionInternal.result){
            menu.add(0,0,0,R.string.execution_information)
        }
        return true
    }

    @Override
    boolean onOptionsItemSelected(MenuItem item) {
        switch (item.itemId){
            case 0:
                def result=(GrooidShell.EvalResult)executionInternal.result
                def closures=result.classes.findAll{Closure.isAssignableFrom(it)}
                new AlertDialog.Builder(this).with {
                    title=R.string.execution_information
                    def sb=new SpannableStringBuilder()
                    sb.append(resources.getString(R.string.count_class,result.classes.size()))
                    sb.append('\n')
                    sb.append(resources.getString(R.string.count_closure,closures.size()))
                    sb.append('\n'*2)
                    sb.append(resources.getString(R.string.detail_class))
                    result.classes.sort{a,b->a.name<=>b.name}.each {clazz->
                        sb.append(new SpannableStringBuilder('\n'*2).with {
                            setSpan(new AbsoluteSizeSpan(20,true),0,size(),SPAN_EXCLUSIVE_EXCLUSIVE)
                            it
                        })
                        sb.append(new SpannableStringBuilder(clazz.name).with {
                            setSpan(new StyleSpan(Typeface.BOLD),0,size(),SPAN_EXCLUSIVE_EXCLUSIVE)
                            setSpan(new AbsoluteSizeSpan(30,true),0,size(),SPAN_EXCLUSIVE_EXCLUSIVE)
                            it
                        })
                        sb.append('\n')
                        sb.append(new SpannableStringBuilder('extends ').with {
                            setSpan(new StyleSpan(Typeface.ITALIC),0,size(),SPAN_EXCLUSIVE_EXCLUSIVE)
                            it
                        })
                        sb.append(new SpannableStringBuilder(clazz.superclass.name).with {
                            setSpan(new StyleSpan(Typeface.BOLD),0,size(),SPAN_EXCLUSIVE_EXCLUSIVE)
                            it
                        })
                        if(clazz.interfaces&&clazz.interfaces.size()) {
                            sb.append('\n')
                            sb.append(new SpannableStringBuilder('implements ').with {
                                setSpan(new StyleSpan(Typeface.ITALIC), 0, size(), SPAN_EXCLUSIVE_EXCLUSIVE)
                                it
                            })
                            def first = true
                            clazz.interfaces.each { interfaze ->
                                if (first) {
                                    first = false
                                } else {
                                    sb.append(', ')
                                }
                                sb.append(new SpannableStringBuilder(interfaze.name).with {
                                    setSpan(new StyleSpan(Typeface.BOLD), 0, size(), SPAN_EXCLUSIVE_EXCLUSIVE)
                                    it
                                })
                            }
                        }
                        //sb.append('\n')
                    }
                    // TODO: add what I need/want
                    message=sb
                    show()
                }.with {
                    def lp=new WindowManager.LayoutParams()
                    lp.copyFrom(window.attributes)
                    lp.width=lp.height=WindowManager.LayoutParams.MATCH_PARENT
                    window.attributes=lp
                }
                break
        }
        return false
    }

    class ErrorDialog extends BottomSheetDialog{
        private replaceLI

        ErrorDialog() {
            super(ExecutionActivity.this)
            replaceLI=true
            setContentView(R.layout.execution_error_dialog)
            replaceLI=false
            ((NestedScrollView)findViewById(R.id.scroll)).fullScroll(NestedScrollView.FOCUS_UP)
        }

        @Override
        LayoutInflater getLayoutInflater() {
            if(replaceLI){
                replaceLI=false
                return ExecutionActivity.this.layoutInflater
            }
            return super.getLayoutInflater()
        }

        void setThrowable(Throwable e){
            assert e
            def sw=new StringWriter()
            def pw=sw.newPrintWriter()
            e.printStackTrace(pw)
            pw.flush()
            ((TextView)findViewById(R.id.error)).text=sw.toString()
        }

        void setTitle(String title){
            ((Toolbar)findViewById(R.id.toolbar)).title=title
        }

        void setTitle(int title){
            ((Toolbar)findViewById(R.id.toolbar)).title=title
        }

        @Override
        protected void onStop() {
            super.onStop()
            //finish()
        }
    }

    class StreamBucket extends Writer{
        boolean error

        @Override
        synchronized void write(char[] cbuf, int off, int len) throws IOException {
            def str=new String(cbuf,off,len)
            def ssb=new SpannableStringBuilder()
            ssb.append(str)
            ssb.setSpan(new ForegroundColorSpan((int)(error?Color.RED:Color.BLACK)),0,str.size(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            runOnUiThread{
                console.append ssb
            }
        }

        @Override
        void flush() throws IOException {

        }

        @Override
        void close() throws IOException {

        }
    }
}
