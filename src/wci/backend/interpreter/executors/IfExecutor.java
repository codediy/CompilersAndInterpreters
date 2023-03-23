package wci.backend.interpreter.executors;

import wci.backend.interpreter.Executor;
import wci.intermediate.ICodeNode;

import java.util.ArrayList;

public class IfExecutor extends StatementExecutor {
    public IfExecutor(Executor parent) {
        super(parent);
    }

    @Override
    public Object execute(ICodeNode node) {
        ArrayList<ICodeNode> children = node.getChildren();
        ICodeNode exprNode = children.get(0);
        ICodeNode thenStmtNode = children.get(1);
        ICodeNode elseStmtNode = children.size() > 2 ? children.get(2) : null;

        ExpressionExecutor expressionExecutor = new ExpressionExecutor(this);
        StatementExecutor statementExecutor = new StatementExecutor(this);

        //表达式计算
        boolean b = (Boolean) expressionExecutor.execute(exprNode);
        if (b) {
            statementExecutor.execute(thenStmtNode);
        } else if (elseStmtNode != null) {
            statementExecutor.execute(elseStmtNode);
        }

        executionCount = executionCount + 1;
        return null;
    }
}
