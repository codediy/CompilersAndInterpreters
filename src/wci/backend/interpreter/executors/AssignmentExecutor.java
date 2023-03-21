package wci.backend.interpreter.executors;

import wci.backend.interpreter.Executor;
import wci.intermediate.ICodeNode;
import wci.intermediate.SymTabEntry;
import wci.intermediate.icodeimpl.ICodeKeyImpl;
import wci.intermediate.symtabimpl.SymTabKeyImpl;
import wci.message.Message;
import wci.message.MessageType;

import java.util.ArrayList;

public class AssignmentExecutor extends StatementExecutor {
    public AssignmentExecutor(Executor parent) {
        super(parent);
    }

    public Object execute(ICodeNode node) {
        ArrayList<ICodeNode> children = node.getChildren();

        ICodeNode variableNode = children.get(0);
        ICodeNode expressionNode = children.get(1);

        ExpressionExecutor expressionExecutor = new ExpressionExecutor(this);
        Object value = expressionExecutor.execute(expressionNode);

        //记录符号的值到符号表
        SymTabEntry variableId = (SymTabEntry) variableNode.getAttribute(ICodeKeyImpl.ID);
        variableId.setAttribute(SymTabKeyImpl.DATA_VALUE, value);

        sendMessage(node, variableId.getName(), value);

        executionCount = executionCount + 1;

        return null;
    }

    private void sendMessage(
            ICodeNode node,
            String variableName,
            Object value
    ) {
        Object linenumber = node.getAttribute(ICodeKeyImpl.LINE);

        if (linenumber != null) {
            sendMessage(new Message(
                    MessageType.ASSIGN,
                    new Object[]{
                            linenumber,
                            variableName,
                            value
                    }
            ));
        }
    }
}
