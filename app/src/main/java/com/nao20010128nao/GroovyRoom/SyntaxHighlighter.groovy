package com.nao20010128nao.GroovyRoom

import android.graphics.Color
import android.util.Log
import groovy.transform.Memoized

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by nao on 2017/05/12.
 */
@Deprecated
class SyntaxHighlighter {
    static final List<Item> DEFAULT_ITEMS=Collections.unmodifiableList([
            // quotes
            new Item('\\".*?\\"'),
            new Item('\\\'.*?\\\''),
            new Item('\\"{3}.*\\"{3}',Pattern.MULTILINE),
            new Item('\\\'{3}.*\\\'{3}',Pattern.MULTILINE),
            // def
            new Item('def ',Pattern.LITERAL),
            // number
            new Item('(?:|\\-)(?:0x[0-9A-Fa-f]+|0b[10]+|0o[0-7]+|[0-9]+)'),
            // primitive types
            new Item('void|byte|short|int|long|float|double'),
            // modifiers
            new Item('final|private|public|static|protected'),
            // keywords
            new Item('class|this|throw|new|break|continue|while|for|do|until'),
            // class-name-ish identifier
            new Item('[~\\*][A-Z][a-zA-Z0-9]+'),
            // import and package statement
            new Item('(?:import|package) [a-zA-Z0-9]*(?:\\.[a-zA-Z0-9]*)*(?:\\.\\*)?'),
            // closure
            new Item('(\\{).*->.*\\}',Pattern.MULTILINE,1),
            new Item('\\{.*(->).*\\}',Pattern.MULTILINE,1),
            new Item('\\{.*->.*(\\})',Pattern.MULTILINE,1),
    ]).with {list->
        def _360=BigDecimal.valueOf(360)
        def diff=_360/size()
        Log.d('SyntaxHighlighter',"diff: $diff")
        for(int i=0;i<size();i++){
            list[i].color=Color.HSVToColor(JavaUtils.toFloatArray([i*diff, 1f, 0.5f]))
        }
        Collections.unmodifiableList(list)
    }

    static class Item{
        final String pattern
        final int flags,group
        int color= Color.BLACK// set it once here
        Item(String pattern,int flags=0,int group=0){
            this.pattern=pattern
            this.flags=flags
            this.group=group
        }
        @Memoized
        Pattern createPattern(){Pattern.compile(pattern,flags)}
        private Matcher matcher(String s){createPattern().matcher(s)}
        void each(String text,Closure processor){
            def match=matcher(text)
            while (match.find()){
                switch (processor.maximumNumberOfParameters){
                    case 1:
                        processor([match.start(group),match.end(group)])
                        break
                    case 2:
                        processor(match.start(group),match.end(group))
                        break
                    case 3:
                        processor(match.start(group),match.end(group),match.group(group))
                        break
                    default:
                        throw new IllegalArgumentException("${processor.maximumNumberOfParameters==0?'Not enough':'Too much'} arguments")
                }
            }
        }
    }
}
