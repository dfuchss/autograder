package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.ForLoopRange;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.VariableUtil;
import de.firemage.autograder.core.integrated.StatementUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.TypeUtil;
import de.firemage.autograder.core.integrated.UsesFinder;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtArrayWrite;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtVariable;

import java.util.List;
import java.util.Map;

@ExecutableCheck(reportedProblems = {ProblemType.COMMON_REIMPLEMENTATION_ARRAYS_FILL})
public class UseArraysFill extends IntegratedCheck {

    private void checkArraysFill(CtFor ctFor) {
        ForLoopRange forLoopRange = ForLoopRange.fromCtFor(ctFor).orElse(null);

        List<CtStatement> statements = StatementUtil.getEffectiveStatements(ctFor.getBody());

        if (statements.size() != 1
            || forLoopRange == null
            || !(statements.get(0) instanceof CtAssignment<?, ?> ctAssignment)
            || !(ctAssignment.getAssigned() instanceof CtArrayWrite<?> ctArrayWrite)
            || !(ctArrayWrite.getIndexExpression() instanceof CtVariableRead<?> index)
            || !(index.getVariable().equals(forLoopRange.loopVariable()))) {
            return;
        }

        CtVariable<?> loopVariable = (CtVariable<?>) VariableUtil.getReferenceDeclaration(forLoopRange.loopVariable());
        // return if the for loop uses the loop variable (would not be a simple repetition)
        if (UsesFinder.variableUses(loopVariable).nestedIn(ctAssignment.getAssignment()).hasAny()) {
            return;
        }

        // ignore new array or new class assignments
        if (ctAssignment.getAssignment() instanceof CtNewClass<?> || ctAssignment.getAssignment() instanceof CtNewArray<?>) {
            return;
        }

        CtExpression<?> rhs = ctAssignment.getAssignment();
        if (!TypeUtil.isImmutable(rhs.getType())) {
            return;
        }

        String suggestion = "Arrays.fill(%s, %s, %s, %s)".formatted(
            ctArrayWrite.getTarget(),
            forLoopRange.start(),
            forLoopRange.end(),
            ctAssignment.getAssignment()
        );
        if (forLoopRange.start() instanceof CtLiteral<Integer> ctLiteral
            && ctLiteral.getValue() == 0
            && forLoopRange.end() instanceof CtFieldAccess<Integer> fieldAccess
            && ctArrayWrite.getTarget().equals(fieldAccess.getTarget())
            && fieldAccess.getVariable().getSimpleName().equals("length")) {
            suggestion = "Arrays.fill(%s, %s)".formatted(
                ctArrayWrite.getTarget(),
                ctAssignment.getAssignment()
            );
        }

        this.addLocalProblem(
            ctFor,
            new LocalizedMessage(
                "common-reimplementation",
                Map.of("suggestion", suggestion)
            ),
            ProblemType.COMMON_REIMPLEMENTATION_ARRAYS_FILL
        );
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        if (!staticAnalysis.hasJavaUtilImport()) {
            return;
        }

        staticAnalysis.processWith(new AbstractProcessor<CtFor>() {
            @Override
            public void process(CtFor ctFor) {
                if (ctFor.isImplicit() || !ctFor.getPosition().isValidPosition()) {
                    return;
                }

                checkArraysFill(ctFor);
            }
        });
    }
}
