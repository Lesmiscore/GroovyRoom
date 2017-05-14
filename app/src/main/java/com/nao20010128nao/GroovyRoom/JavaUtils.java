package com.nao20010128nao.GroovyRoom;

import java.util.List;

/**
 * Created by nao on 2017/05/13.
 */
public class JavaUtils {
    public static float[] toFloatArray(List<Number> num){
        float[] array=new float[num.size()];
        for(int i=0;i<num.size();i++){
            array[i]=num.get(i)!=null?num.get(i).floatValue():Float.NaN;
        }
        return array;
    }
}
