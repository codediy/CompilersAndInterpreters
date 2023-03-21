package wci.backend.interpreter.executors;

import wci.backend.interpreter.Executor;
import wci.intermediate.ICodeNode;

import java.util.ArrayList;

public class CompoundExecutor extends StatementExecutor {
    public CompoundExecutor(Executor parent) {
        super(parent);
    }

    public Object execute(ICodeNode node) {
        StatementExecutor statementExecutor = new StatementExecutor(this);
        ArrayList<ICodeNode> children = node.getChildren();

        for (ICodeNode child : children) {
            statementExecutor.execute(child);
        }

        return null;
    }
}
