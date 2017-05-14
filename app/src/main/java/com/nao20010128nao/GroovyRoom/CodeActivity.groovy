package com.nao20010128nao.GroovyRoom

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import com.nao20010128nao.GroovyRoom.settings.ConfigManager
import com.nao20010128nao.GroovyRoom.view.TextInsertView
import groovy.transform.CompileStatic;

import static com.nao20010128nao.GroovyRoom.Constants.*

@CompileStatic
class CodeActivity extends AppCompatActivity {
    EditText code
    SavesManager saves
    TextInsertView inserter
    ConfigManager cfg
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        setContentView R.layout.activity_code
        saves=new SavesManager(this)
        cfg=new ConfigManager(this)
        code=(EditText)findViewById(R.id.code)
        inserter=(TextInsertView)findViewById(R.id.inserter)
        switch(intent.action){
            case ACTION_NEW:
                break// do nothing
            case ACTION_LOAD:
                def saveName=intent.getStringExtra(EXTRA_CODE_SAVE)
                assert saveName!=null
                title="$title - $saveName"
                code.text=saves[saveName]
                break
            default:
                finish()
        }
        inserter.list=cfg.textInsertStrings
        inserter.linkTextView(code)

        /*if(0) {// stop using it but keep code for the future
            def editing = false
            def editCallback = { Editable s ->
                if (editing) return
                editing = true
                s.clearSpans()
                def range = new ArrayList(0..(s.length() - 1))
                SyntaxHighlighter.DEFAULT_ITEMS.each { item ->
                    item.each("$s") { int b, int e ->
                        if (range.intersect((Iterable) (b..e)).empty) {
                            return
                        }
                        range.removeAll(b..e)
                        Log.d('CodeActivity', "color: $item.color, pattern: $item.pattern, begin: $b, end: $e")
                        s.setSpan(new ForegroundColorSpan(item.color), b, e, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
                editing = false
            }
            editCallback(code.editableText)
            code.addTextChangedListener(new TextWatcher() {
                @Override
                void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                void afterTextChanged(Editable s) {
                    editCallback(s)
                }
            })
        }*/
    }

    @Override
    boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,0,0,R.string.save)
        menu.addSubMenu(0,1,1,R.string.run).with {
            add(1,2,0,R.string.run_normally)
            add(1,3,0,R.string.run_config_slurper)
        }
        return true
    }

    @Override
    boolean onOptionsItemSelected(MenuItem item) {
        switch(item.itemId){
            case 0://save
                startActivity new Intent(this,SaveActivity).putExtra(EXTRA_CODE,"$code.text"as String)
                return true
            case 2://run normally
                startActivity new Intent(this,ExecutionActivity).putExtra(EXTRA_MODE,EXECUTION_MODE_NORMALLY).putExtra(EXTRA_CODE,"$code.text"as String)
                return true
            case 3://run(ConfigSlurper)
                startActivity new Intent(this,ExecutionActivity).putExtra(EXTRA_MODE,EXECUTION_MODE_CONFIGSLURPER).putExtra(EXTRA_CODE,"$code.text"as String)
                return true
        }
        return false
    }
}
