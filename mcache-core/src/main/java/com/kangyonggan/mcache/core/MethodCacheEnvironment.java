package com.kangyonggan.mcache.core;

import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.ProcessingEnvironment;

/**
 * @author kangyonggan
 * @since 10/31/17
 */
public class MethodCacheEnvironment {

    private static MethodCacheEnvironment instance;
    private Trees trees;
    private TreeMaker treeMaker;
    private Name.Table names;

    private MethodCacheEnvironment() {
    }

    public static MethodCacheEnvironment getInstance() {
        if (instance == null) {
            synchronized (MethodCacheEnvironment.class) {
                if (instance == null) {
                    instance = new MethodCacheEnvironment();
                }
            }
        }

        return instance;
    }

    public void init(ProcessingEnvironment env) {
        trees = Trees.instance(env);
        Context context = ((JavacProcessingEnvironment) env).getContext();
        treeMaker = TreeMaker.instance(context);
        names = Names.instance(context).table;
    }

    public Trees getTrees() {
        return trees;
    }

    public TreeMaker getTreeMaker() {
        return treeMaker;
    }

    public Name.Table getNames() {
        return names;
    }
}
