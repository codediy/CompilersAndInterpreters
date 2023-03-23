package wci.backend.interpreter.executors;

import wci.backend.interpreter.Executor;
import wci.intermediate.ICodeNode;
import wci.intermediate.icodeimpl.ICodeKeyImpl;

import java.util.ArrayList;

public class SelectExecutor extends StatementExecutor {
    public SelectExecutor(Executor parent) {
        super(parent);
    }

    @Override
    public Object execute(ICodeNode node) {
        ArrayList<ICodeNode> selectChildren = node.getChildren();
        ICodeNode exprNode = selectChildren.get(0);

        //case的值
        ExpressionExecutor expressionExecutor = new ExpressionExecutor(this);
        Object selectValue = expressionExecutor.execute(exprNode);

        //查找合理的分支
        ICodeNode selectedBranchNode = searchBranches(selectValue, selectChildren);
        if (selectedBranchNode != null) {
            ICodeNode stmtNode = selectedBranchNode.getChildren().get(1);
            StatementExecutor statementExecutor = new StatementExecutor(this);
            statementExecutor.execute(stmtNode);
        }

        executionCount = executionCount + 1;
        return null;
    }

    private ICodeNode searchBranches(Object selectValue, ArrayList<ICodeNode> selectChildren) {
        for (int i = 1; i < selectChildren.size(); i = i + 1) {
            ICodeNode branchNode = selectChildren.get(i);
            if (searchConstants(selectValue, branchNode)) {
                return branchNode;
            }
        }
        return null;
    }

    private boolean searchConstants(Object selectValue, ICodeNode branchNode) {

        boolean integerMode = selectValue instanceof Integer;

        ICodeNode constantsNode = branchNode.getChildren().get(0);
        ArrayList<ICodeNode> constantsList = constantsNode.getChildren();

        if (selectValue instanceof Integer) {
            for (ICodeNode constantNode : constantsList) {
                int constant = (Integer) constantNode.getAttribute(
                        ICodeKeyImpl.VALUE);
                if (((Integer) selectValue) == constant) {
                    return true;
                }
            }
        } else {
            for (ICodeNode constantNode : constantsList) {
                String constant = (String) constantNode.getAttribute(
                        ICodeKeyImpl.VALUE);
                if (((String) selectValue).equals(constant)) {
                    return true;
                }
            }
        }

        return false;
    }
}
