package com.kangyonggan.mcache.core;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

/**
 * JCTree Util
 *
 * @author kangyonggan
 * @since 10/31/17
 */
public class JCTreeUtil {

    /**
     * JCTree environment
     */
    private static MethodCacheEnvironment env = MethodCacheEnvironment.getInstance();

    /**
     * import a package
     *
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
     * define a variable, e.g. private User _user = new User(1001, "zhangsan");
     *
     * @param element
     * @param className
     * @param args
     */
    public static void defineVariable(Element element, String className, List<JCTree.JCExpression> args) {
        JCTree tree = (JCTree) env.getTrees().getTree(element.getEnclosingElement());
        tree.accept(new TreeTranslator() {
            @Override
            public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                String varName = MethodCacheConstants.VARIABLE_PREFIX + className.substring(0, 1).toLowerCase() + className.substring(1);
                boolean hasVariable = hasVariable(translate(jcClassDecl.defs), className, varName);

                ListBuffer<JCTree> statements = new ListBuffer();
                for (JCTree jcTree : jcClassDecl.defs) {
                    statements.append(jcTree);
                }

                if (!hasVariable) {
                    JCTree.JCExpression typeExpr = env.getTreeMaker().Ident(env.getNames().fromString(className));
                    JCTree.JCNewClass newClassExpr = env.getTreeMaker().NewClass(null, List.nil(), typeExpr, args, null);

                    int modifiers = Flags.PRIVATE;
                    // not inner class, variable is static
                    if (jcClassDecl.sym.flatname.toString().equals(jcClassDecl.sym.fullname.toString())) {
                        modifiers = modifiers | Flags.STATIC;
                    }
                    JCTree.JCVariableDecl variableDecl = env.getTreeMaker().VarDef(env.getTreeMaker().Modifiers(modifiers), env.getNames().fromString(varName), typeExpr, newClassExpr);

                    statements.append(variableDecl);

                    jcClassDecl.defs = statements.toList();
                }

                super.visitClassDef(jcClassDecl);
            }
        });
    }

    /**
     * get annotation mirror
     *
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
     * get the method's return type
     *
     * @param element
     * @return
     */
    public static JCTree.JCExpression getReturnType(Element element) {
        final JCTree.JCExpression[] returnType = new JCTree.JCExpression[1];

        JCTree tree = (JCTree) env.getTrees().getTree(element);
        tree.accept(new TreeTranslator() {
            @Override
            public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {
                returnType[0] = jcMethodDecl.restype;
                super.visitMethodDef(jcMethodDecl);
            }
        });

        return returnType[0];
    }

    /**
     * get method's arguments
     *
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
     * adjust class already has variable
     *
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
     * get the method's parameters
     *
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


    /**
     * @param element
     * @param name
     * @return
     */
    public static String getAnnotationParameter(Element element, String name) {
        AnnotationMirror annotationMirror = JCTreeUtil.getAnnotationMirror(element, MethodCache.class.getName());

        for (ExecutableElement ee : annotationMirror.getElementValues().keySet()) {
            if (ee.getSimpleName().toString().equals(name)) {
                return annotationMirror.getElementValues().get(ee).getValue().toString();
            }
        }

        return null;
    }

    /**
     * @param element
     * @param name
     * @param defaultValue
     * @return
     */
    public static String getAnnotationParameter(Element element, String name, String defaultValue) {
        AnnotationMirror annotationMirror = JCTreeUtil.getAnnotationMirror(element, MethodCache.class.getName());

        for (ExecutableElement ee : annotationMirror.getElementValues().keySet()) {
            if (ee.getSimpleName().toString().equals(name)) {
                return annotationMirror.getElementValues().get(ee).getValue().toString();
            }
        }

        return defaultValue;
    }

    /**
     * create code like：Object _cacheValue = _memoryCacheHandle.set(key, _returnValue, expire, unit);
     */
    public static JCTree.JCVariableDecl callMethodWithReturn(String varType, String varName, String targetVarName, String methodName, List args) {
        JCTree.JCIdent varIdent = env.getTreeMaker().Ident(env.getNames().fromString(varName));
        JCTree.JCExpression typeExpr = env.getTreeMaker().Ident(env.getNames().fromString(varType));
        JCTree.JCFieldAccess fieldAccess = env.getTreeMaker().Select(varIdent, env.getNames().fromString(methodName));

        JCTree.JCMethodInvocation methodInvocation = env.getTreeMaker().Apply(List.nil(), fieldAccess, args);
        return env.getTreeMaker().VarDef(env.getTreeMaker().Modifiers(0), env.getNames().fromString(targetVarName), typeExpr, methodInvocation);
    }

    /**
     * get a null value variable
     *
     * @return
     */
    public static JCTree.JCLiteral getNull() {
        return env.getTreeMaker().Literal(TypeTag.BOT, null);
    }

    /**
     * var != null
     *
     * @return
     */
    public static JCTree.JCExpression neNull(String varName) {
        return env.getTreeMaker().Binary(JCTree.Tag.NE, env.getTreeMaker().Ident(env.getNames().fromString(varName)), env.getTreeMaker().Literal(TypeTag.BOT, null));
    }

    /**
     * create code like：_memoryCacheHandle.set(key, _returnValue, expire, unit);
     *
     * @param targetVarName
     * @param methodName
     * @param args
     * @return
     */
    public static JCTree.JCExpressionStatement callMethod(String targetVarName, String methodName, List args) {
        JCTree.JCFieldAccess fieldAccess = env.getTreeMaker().Select(env.getTreeMaker().Ident(env.getNames().fromString(targetVarName)), env.getNames().fromString(methodName));
        JCTree.JCMethodInvocation methodInvocation = env.getTreeMaker().Apply(List.nil(), fieldAccess, args);
        return env.getTreeMaker().Exec(methodInvocation);
    }

}
