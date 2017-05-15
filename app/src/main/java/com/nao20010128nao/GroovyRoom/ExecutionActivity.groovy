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
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.annotation.StyleRes
import android.support.design.widget.BottomSheetDialog
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ContextThemeWrapper
import android.support.v7.widget.Toolbar
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.stream.JsonWriter
import com.nao20010128nao.GroovyRoom.settings.ConfigManager
import groovy.transform.CompileStatic
import me.champeau.groovydroid.GrooidShell
import org.apache.commons.io.output.WriterOutputStream

import static com.nao20010128nao.GroovyRoom.Constants.*

@CompileStatic
class ExecutionActivity extends AppCompatActivity {
    GrooidShell shell
    TextView console
    def consoleLock=new Object()

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
        new Thread({
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
                    jw.setIndent(" " * 4)
                    new Gson().toJson(cs.parse(script), ConfigObject, jw)
                    jw.flush()
                    runOnUiThread {
                        console.text = output.toString()
                    }
                }
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
            }
        }).start()
    }

    @Override
    void onBackPressed() {}

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
