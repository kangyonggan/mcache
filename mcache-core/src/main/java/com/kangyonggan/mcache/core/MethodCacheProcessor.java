package com.kangyonggan.mcache.core;

import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import java.util.Set;

/**
 * @author kangyonggan
 * @since 10/31/17
 */
@SupportedAnnotationTypes("com.kangyonggan.mcache.core.MethodCache")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MethodCacheProcessor extends AbstractProcessor {

    private static MethodCacheEnvironment environment = MethodCacheEnvironment.getInstance();

    /**
     * @param env
     */
    @Override
    public synchronized void init(ProcessingEnvironment env) {
        environment.init(env);
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
                if (!JCTreeUtil.hasReturnValue(element)) {
                    return true;
                }

                String handlePackage = getAnnotationParameter(element, "handle");
                handlePackage = handlePackage == null ? MemoryCacheHandle.class.getName() : handlePackage;
                JCTreeUtil.importPackage(element, handlePackage);

                String className = handlePackage.substring(handlePackage.lastIndexOf(".") + 1);
                JCTreeUtil.defineVariable(element, className);

                generateCode(element, className);
            }
        }
        return true;
    }

    /**
     * @param element
     * @param name
     * @return
     */
    private String getAnnotationParameter(Element element, String name) {
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
     * @param className
     */
    private void generateCode(Element element, String className) {
        String varName = "_" + className.substring(0, 1).toLowerCase() + className.substring(1);
        JCTree tree = (JCTree) environment.getTrees().getTree(element);

        tree.accept(new TreeTranslator() {
            private static final String CACHE_VALUE = "_cacheValue";
            private static final String RETURN_VALUE = "_returnValue";
            private JCTree.JCExpression returnType;

            @Override
            public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {
                returnType = jcMethodDecl.restype;
                super.visitMethodDef(jcMethodDecl);
            }

            @Override
            public void visitBlock(JCTree.JCBlock tree) {
                ListBuffer<JCTree.JCStatement> statements = new ListBuffer();

                /**
                 * create code：Object _cacheValue = _memoryCacheHandle.get("key");
                 */
                JCTree.JCIdent varIdent = environment.getTreeMaker().Ident(environment.getNames().fromString(varName));
                JCTree.JCExpression typeExpr = environment.getTreeMaker().Ident(environment.getNames().fromString("Object"));
                JCTree.JCFieldAccess fieldAccess = environment.getTreeMaker().Select(varIdent, environment.getNames().fromString("get"));

                String prefix = getAnnotationParameter(element, "prefix");
                String key = getAnnotationParameter(element, "value");
                Name cacheValueName = environment.getNames().fromString(CACHE_VALUE);
                JCTree.JCExpression keyExpression = getKeyExpression(key);
                JCTree.JCExpression prefixExpr = environment.getTreeMaker().Literal(TypeTag.BOT, null);
                if (prefix != null) {
                    prefixExpr = environment.getTreeMaker().Ident(environment.getNames().fromString(prefix));
                }
                JCTree.JCMethodInvocation methodInvocation = environment.getTreeMaker().Apply(List.nil(), fieldAccess, List.of(prefixExpr, keyExpression));
                JCTree.JCVariableDecl variableDecl = environment.getTreeMaker().VarDef(environment.getTreeMaker().Modifiers(0), cacheValueName, typeExpr, methodInvocation);
                statements.append(variableDecl);

                /**
                 * create code：if (_cacheValue != null) return (returnType)_cacheValue;
                 */
                JCTree.JCExpression conditionExpression = environment.getTreeMaker().Binary(JCTree.Tag.NE, environment.getTreeMaker().Ident(cacheValueName), environment.getTreeMaker().Literal(TypeTag.BOT, null));
                JCTree.JCParens condition = environment.getTreeMaker().Parens(conditionExpression);
                JCTree.JCStatement statement = environment.getTreeMaker().Return(environment.getTreeMaker().TypeCast(returnType, environment.getTreeMaker().Ident(cacheValueName)));
                JCTree.JCIf jcIf = environment.getTreeMaker().If(condition, statement, null);
                statements.append(jcIf);

                for (int i = 0; i < tree.getStatements().size(); i++) {
                    JCTree.JCStatement jcStatement = tree.getStatements().get(i);

                    if (jcStatement instanceof JCTree.JCReturn) {
                        /**
                         * create code：Object _returnValue = xxx;
                         */
                        JCTree.JCReturn jcReturn = (JCTree.JCReturn) jcStatement;
                        typeExpr = environment.getTreeMaker().Ident(environment.getNames().fromString("Object"));
                        variableDecl = environment.getTreeMaker().VarDef(environment.getTreeMaker().Modifiers(0), environment.getNames().fromString(RETURN_VALUE), typeExpr, jcReturn.getExpression());
                        statements.append(variableDecl);

                        /**
                         * create code：_memoryCacheHandle.set("key", _returnValue, expire, unit);
                         */
                        fieldAccess = environment.getTreeMaker().Select(varIdent, environment.getNames().fromString("set"));
                        String strExpire = getAnnotationParameter(element, "expire");

                        JCTree.JCLiteral literal;
                        if (strExpire != null) {
                            literal = environment.getTreeMaker().Literal(Long.parseLong(strExpire));
                        } else {
                            literal = environment.getTreeMaker().Literal(TypeTag.BOT, null);
                        }
                        String unit = getAnnotationParameter(element, "unit");

                        JCTree.JCFieldAccess fa;
                        if (unit != null) {
                            fa = environment.getTreeMaker().Select(environment.getTreeMaker().Select(environment.getTreeMaker().Ident(environment.getNames().fromString("MethodCache")), environment.getNames().fromString("Unit")), environment.getNames().fromString(unit));
                            methodInvocation = environment.getTreeMaker().Apply(List.nil(), fieldAccess, List.of(prefixExpr, keyExpression, environment.getTreeMaker().Ident(environment.getNames().fromString(RETURN_VALUE)), literal, fa));
                        } else {
                            methodInvocation = environment.getTreeMaker().Apply(List.nil(), fieldAccess, List.of(prefixExpr, keyExpression, environment.getTreeMaker().Ident(environment.getNames().fromString(RETURN_VALUE)), literal, environment.getTreeMaker().Literal(TypeTag.BOT, null)));
                        }
                        JCTree.JCExpressionStatement code = environment.getTreeMaker().Exec(methodInvocation);
                        statements.append(code);

                        /**
                         * create code：return (returnType)_returnValue;
                         */
                        JCTree.JCTypeCast jcTypeCast = environment.getTreeMaker().TypeCast(returnType, environment.getTreeMaker().Ident(environment.getNames().fromString(RETURN_VALUE)));
                        jcReturn.expr = jcTypeCast;
                        statements.append(jcReturn);
                    } else {
                        statements.append(jcStatement);
                    }
                }

                result = environment.getTreeMaker().Block(0, statements.toList());
            }
        });
    }

    /**
     * @param key
     * @return
     */
    private JCTree.JCExpression getKeyExpression(String key) {
        String keys[] = key.split(":");
        JCTree.JCExpression keyExpression = environment.getTreeMaker().Literal("");
        String split = "";

        for (int i = 0; i < keys.length; i++) {
            String k = keys[i];
            if (k.startsWith("$")) {
                k = k.replace("${", "").replace("}", "");
                if (k.contains(".")) {
                    keyExpression = environment.getTreeMaker().Binary(JCTree.Tag.PLUS, keyExpression, environment.getTreeMaker().Literal(split));
                    keyExpression = environment.getTreeMaker().Binary(JCTree.Tag.PLUS, keyExpression, JCTreeUtil.buildGetter(k));
                } else {
                    keyExpression = environment.getTreeMaker().Binary(JCTree.Tag.PLUS, keyExpression, environment.getTreeMaker().Literal(split));
                    JCTree.JCIdent ident = environment.getTreeMaker().Ident(environment.getNames().fromString(k));
                    keyExpression = environment.getTreeMaker().Binary(JCTree.Tag.PLUS, keyExpression, ident);
                }
            } else {
                keyExpression = environment.getTreeMaker().Binary(JCTree.Tag.PLUS, keyExpression, environment.getTreeMaker().Literal(split + k));
            }

            split = ":";
        }

        return keyExpression;
    }
}
