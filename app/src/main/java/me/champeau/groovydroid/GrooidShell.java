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

package me.champeau.groovydroid;

import android.os.Build;
import android.util.Log;

import com.android.dx.Version;
import com.android.dx.cf.direct.DirectClassFile;
import com.android.dx.dex.DexOptions;
import com.android.dx.dex.cf.CfOptions;
import com.android.dx.dex.cf.CfTranslator;
import com.android.dx.dex.code.PositionList;
import com.android.dx.dex.file.ClassDefItem;
import com.android.dx.dex.file.DexFile;

import org.codehaus.groovy.control.BytecodeProcessor;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import dalvik.system.DexClassLoader;
import groovy.lang.GrooidClassLoader;
import groovy.lang.Script;

/**
 * A shell capable of executing Groovy scripts at runtime, on an Android device.
 *
 * @author Cédric Champeau
 */
public class GrooidShell {

    private static final String DEX_IN_JAR_NAME = "classes.dex";
    private static final Attributes.Name CREATED_BY = new Attributes.Name("Created-By");

    private final DexOptions dexOptions;
    private final CfOptions cfOptions;

    private final File tmpDynamicFiles;
    private final ClassLoader classLoader;

    public GrooidShell(File tmpDir, ClassLoader parent) {
        tmpDynamicFiles = tmpDir;
        classLoader = parent;
        dexOptions = new DexOptions();
        dexOptions.targetApiLevel = 13;
        cfOptions = new CfOptions();
        cfOptions.positionInfo = PositionList.LINES;
        cfOptions.localInfo = true;
        cfOptions.strictNameCheck = true;
        cfOptions.optimize = false;
        cfOptions.optimizeListFile = null;
        cfOptions.dontOptimizeListFile = null;
        cfOptions.statistics = false;
    }


    public EvalResult evaluate(String scriptText,EvalProgress progress) throws Throwable{
        progress.onProgress(EvalProgressCategory.COMPILING);
        long sd = System.nanoTime();
        final Set<String> classNames = new LinkedHashSet<String>();
        final DexFile dexFile = new DexFile(dexOptions);
        CompilerConfiguration config = new CompilerConfiguration();
        config.setBytecodePostprocessor(new BytecodeProcessor() {
            @Override
            public byte[] processBytecode(String s, byte[] bytes) {
                //ClassDefItem classDefItem = CfTranslator.translate(new DirectClassFile(bytes, s+".class", false), bytes, cfOptions, dexOptions, dexFile);
                ClassDefItem classDefItem = CfTranslator.translate(s+".class", bytes, cfOptions, dexOptions);
                dexFile.add(classDefItem);
                classNames.add(s);
                return bytes;
            }
        });

        GrooidClassLoader gcl = new GrooidClassLoader(this.classLoader, config);
        try {
            gcl.parseClass(scriptText);
        }  catch (Throwable e) {
            Log.e("GrooidShell","Dynamic loading failed!",e);
            throw e;
        }
        byte[] dalvikBytecode;
        try {
            dalvikBytecode = dexFile.toDex(new OutputStreamWriter(new ByteArrayOutputStream()), false);
        } catch (IOException e) {
            Log.e("GrooidShell", "Unable to convert to Dalvik", e);
            throw e;
        }

        progress.onProgress(EvalProgressCategory.DEXING);
        Map<String, Class> classes = defineDynamic(classNames, dalvikBytecode);
        long compilationTime = System.nanoTime()-sd;
        long execTime = 0;
        Script script = null;
        for (Class scriptClass : classes.values()) {
            if (Script.class.isAssignableFrom(scriptClass)) {
                sd = System.nanoTime();
                try {
                    script = (Script) scriptClass.newInstance();
                } catch (Throwable e) {
                    Log.e("GroovyDroidShell", "Unable to create script",e);
                    throw e;
                }
                execTime = System.nanoTime()-sd;
                break;
            }
        }
        return new EvalResult(compilationTime, execTime, script, new ArrayList<>(classes.values()));
    }


    private Map<String,Class> defineDynamic(Set<String> classNames, byte[] dalvikBytecode)throws Throwable {
        File tmpDex = new File(tmpDynamicFiles, UUID.randomUUID().toString()+".jar");
        Map<String,Class> result = new LinkedHashMap<String, Class>();
        try {
            FileOutputStream fos = null;
            JarOutputStream jar = null;
            try {
                fos = new FileOutputStream(tmpDex);
                jar = new JarOutputStream(fos, makeManifest());
                JarEntry classes = new JarEntry(DEX_IN_JAR_NAME);
                classes.setSize(dalvikBytecode.length);
                jar.putNextEntry(classes);
                jar.write(dalvikBytecode);
                jar.closeEntry();
                jar.finish();
            }finally{
                if(jar!=null){
                    jar.flush();
                    jar.close();
                }
                if(fos!=null){
                    fos.flush();
                    fos.close();
                }
            }
            DexClassLoader loader = new DexClassLoader(tmpDex.getAbsolutePath(), tmpDynamicFiles.getAbsolutePath(), null, classLoader);
            for (String className : classNames) {
                result.put(className, loader.loadClass(className));
            }
            return result;
        } catch (Throwable e) {
            Log.e("DynamicLoading", "Unable to load class",e);
            throw e;
        } finally {
            tmpDex.delete();
        }
    }

    private static Manifest makeManifest() throws IOException {
        Manifest manifest = new Manifest();
        Attributes attribs = manifest.getMainAttributes();
        attribs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attribs.put(CREATED_BY, "dx " + Version.VERSION);
        attribs.putValue("Dex-Location", DEX_IN_JAR_NAME);
        return manifest;
    }

    public static class EvalResult {
        public final long compilationTime;
        public final long execTime;
        public final Script script;
        public final List<Class> classes;

        public EvalResult(long compilationTime, long execTime, Script script,List<Class> classes) {
            this.compilationTime = compilationTime;
            this.execTime = execTime;
            this.script = script;
            this.classes = classes;
        }
    }

    public interface EvalProgress{
        void onProgress(EvalProgressCategory prog);
    }

    public enum EvalProgressCategory{
        COMPILING,DEXING;
    }
}
