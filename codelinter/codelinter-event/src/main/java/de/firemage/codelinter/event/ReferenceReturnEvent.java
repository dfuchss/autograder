package de.firemage.codelinter.event;

public class ReferenceReturnEvent implements MethodEvent {
    private final String clazz;
    private final String methodName;
    private final String methodDesc;
    private final String returnedClass;

    public ReferenceReturnEvent(String clazz, String methodName, String methodDesc, String returnedClass) {
        this.clazz = clazz;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
        this.returnedClass = returnedClass;
    }

    public String getReturnedClass() {
        return this.returnedClass;
    }

    @Override
    public String getOwningClass() {
        return this.clazz;
    }

    @Override
    public String getMethodName() {
        return this.methodName;
    }

    @Override
    public String getMethodDescriptor() {
        return this.methodDesc;
    }

    @Override
    public String format() {
        return "RefRet:" + this.clazz + ":" + this.methodName + ":" + this.methodDesc + ":" + this.returnedClass;
    }
}
