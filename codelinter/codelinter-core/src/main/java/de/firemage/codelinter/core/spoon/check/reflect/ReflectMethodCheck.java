package de.firemage.codelinter.core.spoon.check.reflect;

import de.firemage.codelinter.core.ProblemCategory;
import de.firemage.codelinter.core.ProblemPriority;
import de.firemage.codelinter.core.spoon.ProblemLogger;
import de.firemage.codelinter.core.spoon.check.AbstractLoggingProcessor;
import de.firemage.codelinter.core.spoon.check.CheckUtil;
import spoon.reflect.code.CtInvocation;

public class ReflectMethodCheck extends AbstractLoggingProcessor<CtInvocation<?>> {
    private static final String DESCRIPTION = "Used the 'Class' type of an object";
    private static final String EXPLANATION = """
            Using Java reflection indicates bad design in the context of programming lectures.
            Boilerplate code is always better then possibly breaking OOP best practices. Reflection includes
            methods like Class.forName(), Class.getMethod(), ...""";

    public ReflectMethodCheck(ProblemLogger logger) {
        super(logger);
    }

    @Override
    public void process(CtInvocation<?> element) {
        //TODO This checks only for uses of methods of java.lang.Class and not for other possible reflection uses
        if (element.getExecutable().getDeclaringType().getQualifiedName().equals("java.lang.Class") &&
                !CheckUtil.isInEquals(element)) {
            addProblem(element, DESCRIPTION, ProblemCategory.JAVA_FEATURE, EXPLANATION, ProblemPriority.SEVERE);
        }
    }
}