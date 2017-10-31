package com.kangyonggan.mcache.core;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

/**
 * @author kangyonggan
 * @since 10/31/17
 */
public class JCTreeUtil {

    private static MethodCacheEnvironment env = MethodCacheEnvironment.getInstance();

    /**
     * @param element
     * @param packageName
     */
    public static void importPackage(Element element, String packageName) {
        JCTree.JCCompilationUnit compilationUnit = (JCTree.JCCompilationUnit) env.getTrees().getPath(element.getEnclosingElement()).getCompilationUnit();
        String className = packageName.substring(packageName.lastIndexOf(".") + 1);
        packageName = packageName.substring(0, packageName.lastIndexOf("."));

        JCTree.JCFieldAccess fieldAccess = env.getTreeMaker().Select(env.getTreeMaker().Ident(env.getNames().fromString(packageName)), env.getNames().fromString(className));
        JCTree.JCImport jcImport = env.getTreeMaker().Import(fieldAccess, false);

        ListBuffer<JCTree> imports = new ListBuffer();
        imports.append(jcImport);

        for (int i = 0; i < compilationUnit.defs.size(); i++) {
            imports.append(compilationUnit.defs.get(i));
        }

        compilationUnit.defs = imports.toList();
    }

    /**
     * @param element
     * @param className
     * @param args
     */
    public static void defineVariable(Element element, String className, Object... args) {
        JCTree tree = (JCTree) env.getTrees().getTree(element.getEnclosingElement());
        tree.accept(new TreeTranslator() {
            @Override
            public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                String varName = className.substring(0, 1).toLowerCase() + className.substring(1);
                boolean hasVariable = hasVariable(translate(jcClassDecl.defs), className, varName);

                ListBuffer<JCTree> statements = new ListBuffer();
                for (JCTree jcTree : jcClassDecl.defs) {
                    statements.append(jcTree);
                }

                if (!hasVariable) {
                    JCTree.JCExpression typeExpr = env.getTreeMaker().Ident(env.getNames().fromString(className));
                    JCTree.JCNewClass newClassExpr = env.getTreeMaker().NewClass(null, List.nil(), typeExpr, getArgs(args).toList(), null);
                    JCTree.JCVariableDecl variableDecl = env.getTreeMaker().VarDef(env.getTreeMaker().Modifiers(Flags.PRIVATE), env.getNames().fromString(varName), typeExpr, newClassExpr);
                    statements.append(variableDecl);

                    jcClassDecl.defs = statements.toList();
                }

                super.visitClassDef(jcClassDecl);
            }
        });
    }

    /**
     * @param element
     * @param name
     * @return
     */
    public static AnnotationMirror getAnnotationMirror(Element element, String name) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            if (name.equals(annotationMirror.getAnnotationType().toString())) {
                return annotationMirror;
            }
        }

        return null;
    }

    /**
     * @param element
     * @return
     */
    public static boolean hasReturnValue(Element element) {
        // TODO
        return true;
    }

    /**
     * @param args
     * @return
     */
    public static ListBuffer<JCTree.JCExpression> getArgs(Object... args) {
        ListBuffer<JCTree.JCExpression> argList = new ListBuffer();

        for (Object arg : args) {
            JCTree.JCExpression argsExpr = env.getTreeMaker().Literal(arg);
            argList.append(argsExpr);
        }

        return argList;
    }

    /**
     * @param oldList
     * @param className
     * @param varName
     * @return
     */
    public static boolean hasVariable(List<JCTree> oldList, String className, String varName) {
        boolean hasField = false;

        for (JCTree jcTree : oldList) {
            if (jcTree.getKind() == Tree.Kind.VARIABLE) {
                JCTree.JCVariableDecl variableDecl = (JCTree.JCVariableDecl) jcTree;

                if (varName.equals(variableDecl.name.toString()) && className.equals(variableDecl.vartype.toString())) {
                    hasField = true;
                    break;
                }
            }
        }

        return hasField;
    }

    /**
     * @param jcMethodDecl
     */
    public static List<JCTree.JCExpression> getParameters(JCTree.JCMethodDecl jcMethodDecl) {
        List<JCTree.JCExpression> params = List.nil();
        params = params.append(env.getTreeMaker().Literal(jcMethodDecl.getName().toString()));
        for (JCTree.JCVariableDecl decl : jcMethodDecl.getParameters()) {
            params = params.append(env.getTreeMaker().Ident(decl));
        }

        return params;
    }

}
