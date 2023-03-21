package wci.backend.interpreter;

import wci.backend.Backend;
import wci.intermediate.ICodeNode;
import wci.message.Message;

import static wci.intermediate.icodeimpl.ICodeKeyImpl.LINE;
import static wci.message.MessageType.RUNTIME_ERROR;

public class RuntimeErrorHandler {
    private static final int MAX_ERRORS = 5;

    private static int errorCount = 0;

    public static int getErrorCount() {
        return errorCount;
    }

    public void flag(ICodeNode node, RuntimeErrorCode errorCode,
                     Backend backend) {
        String lineNumber = null;

        while ((node != null) && (node.getAttribute(LINE) == null)) {
            node = node.getParent();
        }

        backend.sendMessage(
                new Message(RUNTIME_ERROR,
                        new Object[]{errorCode.toString(),
                                (Integer) node.getAttribute(LINE)}));

        if (++errorCount > MAX_ERRORS) {
            System.out.println("*** ABORTED AFTER TOO MANY RUNTIME ERRORS.");
            System.exit(-1);
        }
    }
}
