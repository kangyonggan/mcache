package com.kangyonggan.mcache.core;

import com.kangyonggan.mcache.core.express.JCExpressionParser;
import com.sun.source.util.Trees;
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

    private JCExpressionParser parser;
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
        parser = new JCExpressionParser(treeMaker, names);
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
                JCTreeUtil.importPackage(element, MethodReturnHandle.class.getName());

                String className = StringUtil.getClassName(handlePackageName);
                JCTreeUtil.defineVariable(element, className, List.nil());

                generateReturnCode(element, handlePackageName);
                generateBlockCode(element, handlePackageName, className);
            }
        }
        return true;
    }

    /**
     * @param element
     * @param handlePackageName
     */
    private void generateReturnCode(Element element, String handlePackageName) {
        Element parentElement = element.getEnclosingElement();
        JCTree tree = (JCTree) trees.getTree(parentElement);

        tree.accept(new TreeTranslator() {
            private boolean hasAnnotation;

            @Override
            public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {
                super.visitMethodDef(jcMethodDecl);
                hasAnnotation = false;
            }

            @Override
            public void visitAnnotation(JCTree.JCAnnotation jcAnnotation) {
                super.visitAnnotation(jcAnnotation);
                hasAnnotation = true;
            }

            @Override
            public void visitReturn(JCTree.JCReturn jcReturn) {
                if (!hasAnnotation) {
                    super.visitReturn(jcReturn);
                    return;
                }

                /**
                 * create code: MethodReturnHandle.processReturn(args);
                 */
                String key = JCTreeUtil.getAnnotationParameter(element, "value");
                JCTree.JCExpression keyExpression = parser.parse(key);
                String prefix = JCTreeUtil.getAnnotationParameter(element, "prefix");
                JCTree.JCExpression prefixExpr = JCTreeUtil.getNull();
                if (StringUtil.isNotEmpty(prefix)) {
                    prefixExpr = treeMaker.Ident(names.fromString(prefix));
                }
                String strExpire = JCTreeUtil.getAnnotationParameter(element, "expire");
                JCTree.JCLiteral expireLiteral = strExpire == null ? JCTreeUtil.getNull() : treeMaker.Literal(Long.parseLong(strExpire));
                String unit = JCTreeUtil.getAnnotationParameter(element, "unit");
                List args;
                if (unit != null) {
                    JCTree.JCFieldAccess fa = treeMaker.Select(treeMaker.Select(treeMaker.Ident(names.fromString(MethodCache.class.getSimpleName())), names.fromString(MethodCache.Unit.class.getSimpleName())), names.fromString(unit));
                    args = List.of(treeMaker.Literal(handlePackageName), prefixExpr, keyExpression, jcReturn.getExpression(), expireLiteral, fa);
                } else {
                    args = List.of(treeMaker.Literal(handlePackageName), prefixExpr, keyExpression, jcReturn.getExpression(), expireLiteral, JCTreeUtil.getNull());
                }

                JCTree.JCFieldAccess fieldAccess = treeMaker.Select(treeMaker.Ident(names.fromString(MethodReturnHandle.class.getSimpleName())), names.fromString("processReturn"));
                JCTree.JCMethodInvocation methodInvocation = treeMaker.Apply(List.nil(), fieldAccess, args);
                JCTree.JCTypeCast jcTypeCast = treeMaker.TypeCast(JCTreeUtil.getReturnType(element), methodInvocation);
                jcReturn.expr = jcTypeCast;
                this.result = jcReturn;
            }
        });
    }

    /**
     * @param element
     * @param className
     */
    private void generateBlockCode(Element element, String handlePackageName, String className) {
        String varName = MethodCacheConstants.VARIABLE_PREFIX + StringUtil.toVarName(className);
        JCTree tree = (JCTree) trees.getTree(element);

        tree.accept(new TreeTranslator() {
            @Override
            public void visitBlock(JCTree.JCBlock tree) {
                ListBuffer<JCTree.JCStatement> statements = new ListBuffer();

                /**
                 * create code: Object _cacheValue = MethodReturnHandle.get(handlePackageName, prefix, key);
                 */
                String key = JCTreeUtil.getAnnotationParameter(element, "value");
                JCTree.JCExpression keyExpression = parser.parse(key);
                String prefix = JCTreeUtil.getAnnotationParameter(element, "prefix");
                JCTree.JCExpression prefixExpr = JCTreeUtil.getNull();
                if (StringUtil.isNotEmpty(prefix)) {
                    prefixExpr = treeMaker.Ident(names.fromString(prefix));
                }
                statements.append(JCTreeUtil.callMethodWithReturn("Object", MethodReturnHandle.class.getSimpleName(), MethodCacheConstants.VARIABLE_CACHE_VALUE_NAME, "get", List.of(treeMaker.Literal(handlePackageName), prefixExpr, keyExpression)));

                /**
                 * create code：if (_cacheValue != null) {return (returnType)_cacheValue;}
                 */
                JCTree.JCParens condition = treeMaker.Parens(JCTreeUtil.neNull(MethodCacheConstants.VARIABLE_CACHE_VALUE_NAME));
                JCTree.JCStatement statementTrue = treeMaker.Return(treeMaker.TypeCast(JCTreeUtil.getReturnType(element), treeMaker.Ident(names.fromString(MethodCacheConstants.VARIABLE_CACHE_VALUE_NAME))));
                JCTree.JCIf jcIf = treeMaker.If(condition, statementTrue, null);
                statements.append(jcIf);

                for (JCTree.JCStatement jcStatement : tree.getStatements()) {
                    statements.append(jcStatement);
                }

                result = treeMaker.Block(0, statements.toList());
            }
        });
    }
}
