package com.kangyonggan.mcache.core.express;

import com.kangyonggan.mcache.core.StringUtil;
import com.sun.tools.javac.tree.JCTree;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kangyonggan
 * @since 11/2/17
 */
public class JCExpressionParse {

    /**
     * expression's prefix
     */
    private static final String EXPR_PREFIX = "$";

    /**
     * expression's begin
     */
    private static final String EXPR_BEGIN = "{";

    /**
     * expression's end
     */
    private static final String EXPR_END = "}";

    /**
     * expression's get
     */
    private static final String EXPR_GET = ".";

    /**
     * expression's array begin
     */
    private static final String EXPR_ARR_BEGIN = "[";

    /**
     * expression's array end
     */
    private static final String EXPR_ARR_END = "]";

    /**
     * expression's list begin
     */
    private static final String EXPR_LIST_BEGIN = "(";

    /**
     * expression's list end
     */
    private static final String EXPR_LIST_END = ")";

    /**
     * parse expression
     *
     * @param express
     */
    public JCTree.JCExpression parse(String express) {
        StringBuilder exprSign = new StringBuilder();
        List<JCExpressionElement> exprElements = new ArrayList();

        for (int i = 0; i < express.length(); i++) {
            String str = express.substring(i, i + 1);
            switch (str) {
                case EXPR_PREFIX: {
                    break;
                }
                case EXPR_BEGIN: {

                    break;
                }
                case EXPR_END: {

                    break;
                }
                case EXPR_GET: {

                    break;
                }
                default: {
                    exprSign.append(str);
                }
            }
        }

        System.out.println("解析后:" + exprElements);
        return convert(exprElements);
    }

    private JCTree.JCExpression convert(List<JCExpressionElement> exprElements) {
        return null;
    }

    public static void main(String[] args) {
        JCTree.JCExpression jcExpression = new JCExpressionParse().parse("Hello");
        System.out.println(jcExpression);
    }
}
