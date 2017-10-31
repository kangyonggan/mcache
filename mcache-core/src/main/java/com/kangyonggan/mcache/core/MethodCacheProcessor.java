package com.kangyonggan.mcache.core;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;

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

                String handlePackage = getHandlePackage(element);
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
     * @return
     */
    private String getHandlePackage(Element element) {
        String handle = MethodCacheConfig.getMethodCacheHandle().getName();
        AnnotationMirror channelMirror = JCTreeUtil.getAnnotationMirror(element, MethodCache.class.getName());

        for (ExecutableElement ee : channelMirror.getElementValues().keySet()) {
            if (ee.getSimpleName().toString().equals("handle")) {
                ee.getDefaultValue();
                handle = channelMirror.getElementValues().get(ee).getValue().toString();
            }
        }

        return handle;
    }

    /**
     * @param element
     * @param className
     */
    private void generateCode(Element element, String className) {
        JCTree tree = (JCTree) environment.getTrees().getTree(element);

        tree.accept(new TreeTranslator() {
            private List<JCTree.JCExpression> params;
            private JCTree.JCExpression returnType;

            @Override
            public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {
                params = JCTreeUtil.getParameters(jcMethodDecl);
                returnType = jcMethodDecl.restype;

                super.visitMethodDef(jcMethodDecl);
            }

            @Override
            public void visitBlock(JCTree.JCBlock tree) {
                super.visitBlock(tree);
            }
        });
    }


}
