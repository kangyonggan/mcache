package com.kangyonggan.mcache.core;

import com.sun.source.util.Trees;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * annotation processor
 *
 * @author kangyonggan
 * @since 10/31/17
 */
@SupportedAnnotationTypes("com.kangyonggan.mcache.core.MethodCache")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MethodCacheProcessor extends AbstractProcessor {

    private Trees trees;
    private TreeMaker treeMaker;
    private Name.Table names;

    /**
     * @param env
     */
    @Override
    public synchronized void init(ProcessingEnvironment env) {
        MethodCacheEnvironment me = MethodCacheEnvironment.getInstance();
        me.init(env);
        trees = me.getTrees();
        treeMaker = me.getTreeMaker();
        names = me.getNames();
    }

    /**
     * @param annotations
     * @param env
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        for (Element element : env.getElementsAnnotatedWith(MethodCache.class)) {
            if (element.getKind() == ElementKind.METHOD) {
                if (JCTreeUtil.getReturnType(element) == null) {
                    return true;
                }

                String handlePackageName = JCTreeUtil.getAnnotationParameter(element, "handle", MemoryCacheHandle.class.getName());
                JCTreeUtil.importPackage(element, handlePackageName);

                String className = StringUtil.getClassName(handlePackageName);
                JCTreeUtil.defineVariable(element, className, List.nil());

                generateCode(element, className);
            }
        }
        return true;
    }

    /**
     * @param element
     * @param className
     */
    private void generateCode(Element element, String className) {
        String varName = MethodCacheConstants.VARIABLE_PREFIX + StringUtil.toVarName(className);
        JCTree tree = (JCTree) trees.getTree(element);

        tree.accept(new TreeTranslator() {
            @Override
            public void visitBlock(JCTree.JCBlock tree) {
                ListBuffer<JCTree.JCStatement> statements = new ListBuffer();

                /**
                 * create code: Object _cacheValue = _memoryCacheHandle.get(prefix, key);
                 */
                String key = JCTreeUtil.getAnnotationParameter(element, "value");
                JCTree.JCExpression keyExpression = getKeyExpression(key);
                String prefix = JCTreeUtil.getAnnotationParameter(element, "prefix");
                JCTree.JCExpression prefixExpr = JCTreeUtil.getNull();
                if (StringUtil.isNotEmpty(prefix)) {
                    prefixExpr = treeMaker.Ident(names.fromString(prefix));
                }
                statements.append(JCTreeUtil.callMethodWithReturn("Object", varName, MethodCacheConstants.VARIABLE_CACHE_VALUE_NAME, "get", List.of(prefixExpr, keyExpression)));

                /**
                 * create code：if (_cacheValue != null) {return (returnType)_cacheValue;}
                 */
                JCTree.JCParens condition = treeMaker.Parens(JCTreeUtil.neNull(MethodCacheConstants.VARIABLE_CACHE_VALUE_NAME));
                JCTree.JCExpression returnType = JCTreeUtil.getReturnType(element);
                JCTree.JCStatement statementTrue = treeMaker.Return(treeMaker.TypeCast(returnType, treeMaker.Ident(names.fromString(MethodCacheConstants.VARIABLE_CACHE_VALUE_NAME))));
                JCTree.JCIf jcIf = treeMaker.If(condition, statementTrue, null);
                statements.append(jcIf);

                for (JCTree.JCStatement jcStatement : tree.getStatements()) {
                    if (jcStatement instanceof JCTree.JCReturn) {
                        /**
                         * create code：Object _returnValue = xxx;
                         */
                        JCTree.JCReturn jcReturn = (JCTree.JCReturn) jcStatement;
                        Name returnName = names.fromString(MethodCacheConstants.VARIABLE_RETURN_VALUE_NAME);
                        JCTree.JCVariableDecl variableDecl = treeMaker.VarDef(treeMaker.Modifiers(0), returnName, treeMaker.Ident(names.fromString("Object")), jcReturn.getExpression());
                        statements.append(variableDecl);

                        /**
                         * create code：_memoryCacheHandle.set(key, _returnValue, expire, unit);
                         */
                        String strExpire = JCTreeUtil.getAnnotationParameter(element, "expire");
                        JCTree.JCLiteral literal = strExpire == null ? JCTreeUtil.getNull() : treeMaker.Literal(Long.parseLong(strExpire));
                        String unit = JCTreeUtil.getAnnotationParameter(element, "unit");
                        List args;
                        if (unit != null) {
                            JCTree.JCFieldAccess fa = treeMaker.Select(treeMaker.Select(treeMaker.Ident(names.fromString(MethodCache.class.getSimpleName())), names.fromString(MethodCache.Unit.class.getSimpleName())), names.fromString(unit));
                            args = List.of(prefixExpr, keyExpression, treeMaker.Ident(returnName), literal, fa);
                        } else {
                            args = List.of(prefixExpr, keyExpression, treeMaker.Ident(returnName), literal, JCTreeUtil.getNull());
                        }
                        statements.append(JCTreeUtil.callMethod(varName, "set", args));

                        /**
                         * create code：return (returnType)_returnValue;
                         */
                        JCTree.JCTypeCast jcTypeCast = treeMaker.TypeCast(returnType, treeMaker.Ident(names.fromString(MethodCacheConstants.VARIABLE_RETURN_VALUE_NAME)));
                        jcReturn.expr = jcTypeCast;
                        statements.append(jcReturn);
                    } else {
                        statements.append(jcStatement);
                    }
                }

                result = treeMaker.Block(0, statements.toList());
            }
        });
    }

    /**
     * @param key
     * @return
     */
    private JCTree.JCExpression getKeyExpression(String key) {
        String keys[] = key.split(MethodCacheConstants.CACHE_KEY_SPLIT);
        JCTree.JCExpression keyExpression = treeMaker.Literal(StringUtil.EMPTY);
        String split = StringUtil.EMPTY;

        for (String k : keys) {
            if (k.startsWith("$")) {
                k = k.replace("${", StringUtil.EMPTY).replace("}", StringUtil.EMPTY);
                if (k.contains(".")) {
                    keyExpression = treeMaker.Binary(JCTree.Tag.PLUS, keyExpression, treeMaker.Literal(split));
                    keyExpression = treeMaker.Binary(JCTree.Tag.PLUS, keyExpression, buildGetter(k));
                } else {
                    keyExpression = treeMaker.Binary(JCTree.Tag.PLUS, keyExpression, treeMaker.Literal(split));
                    keyExpression = treeMaker.Binary(JCTree.Tag.PLUS, keyExpression, treeMaker.Ident(names.fromString(k)));
                }
            } else {
                keyExpression = treeMaker.Binary(JCTree.Tag.PLUS, keyExpression, treeMaker.Literal(split + k));
            }

            split = MethodCacheConstants.CACHE_KEY_SPLIT;
        }

        return keyExpression;
    }

    /**
     * build key's getter
     *
     * @param key
     * @return
     */
    private JCTree.JCExpression buildGetter(String key) {
        String arr[] = key.split("\\.");

        JCTree.JCExpression expression = treeMaker.Ident(names.fromString(arr[0]));
        for (int i = 1; i < arr.length; i++) {
            String methodName = "get" + StringUtil.toClassName(arr[i]);
            JCTree.JCFieldAccess fieldAccess = treeMaker.Select(expression, names.fromString(methodName));
            expression = treeMaker.Apply(List.nil(), fieldAccess, List.nil());
        }

        return expression;
    }
}
