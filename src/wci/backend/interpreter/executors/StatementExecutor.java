package wci.backend.interpreter.executors;

import wci.backend.interpreter.Executor;
import wci.backend.interpreter.RuntimeErrorCode;
import wci.frontend.pascal.PascalErrorCode;
import wci.intermediate.ICodeNode;
import wci.intermediate.icodeimpl.ICodeKeyImpl;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;
import wci.message.Message;
import wci.message.MessageType;

public class StatementExecutor extends Executor {

    public StatementExecutor(Executor parent) {
        super(parent);
    }

    public Object execute(ICodeNode node) {
        ICodeNodeTypeImpl nodeType = (ICodeNodeTypeImpl) node.getType();

        //打印行数信息
        sendSourceLineMessage(node);

        //执行
        switch (nodeType) {
            case COMPOUND: {
                CompoundExecutor compoundExecutor = new CompoundExecutor(this);
                return compoundExecutor.execute(node);
            }
            case ASSIGN: {
                AssignmentExecutor assignmentExecutor = new AssignmentExecutor(this);
                return assignmentExecutor.execute(node);
            }
            case NO_OP: {
                return null;
            }
            default: {
                errorHandler.flag(node, RuntimeErrorCode.UNIMPLEMENTED_FEATURE, this);
                return null;
            }
        }
    }

    private void sendSourceLineMessage(ICodeNode node) {
        Object lineNumber = node.getAttribute(ICodeKeyImpl.LINE);
        if (lineNumber != null) {
            sendMessage(new Message(
                    MessageType.SOURCE_LINE, lineNumber
            ));
        }
    }
}
