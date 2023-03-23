package wci.backend.interpreter.executors;

import wci.backend.interpreter.Executor;
import wci.intermediate.ICodeNode;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;

import java.util.ArrayList;

public class LoopExecutor extends StatementExecutor {
    public LoopExecutor(Executor parent) {
        super(parent);
    }

    @Override
    public Object execute(ICodeNode node) {
        boolean exitLoop = false;
        ICodeNode exprNode = null;
        ArrayList<ICodeNode> loopChildren = node.getChildren();

        ExpressionExecutor expressionExecutor = new ExpressionExecutor(this);
        StatementExecutor statementExecutor = new StatementExecutor(this);

        while (!exitLoop) {
            executionCount = executionCount + 1;

            for (ICodeNode child : loopChildren) {
                ICodeNodeTypeImpl childType = (ICodeNodeTypeImpl) child.getType();

                if (childType == ICodeNodeTypeImpl.TEST) {
                    if (exprNode == null) {
                        exprNode = child.getChildren().get(0);
                    }
                    exitLoop = (Boolean) expressionExecutor.execute(exprNode);
                } else {
                    //语句
                    statementExecutor.execute(child);
                }

                if (exitLoop) {
                    break;
                }
            }
        }


        return null;
    }
}
