package com.kangyonggan.mcache.core.express;

/**
 * @author kangyonggan
 * @since 11/2/17
 */
public class JCExpressionLiteral extends JCExpressionElement {

    private Object object;

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    @Override
    public String toString() {
        return "JCExpressionLiteral{" +
                "object=" + object +
                '}';
    }
}
