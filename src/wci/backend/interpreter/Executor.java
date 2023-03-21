package wci.backend.interpreter;

import wci.backend.Backend;
import wci.backend.interpreter.executors.StatementExecutor;
import wci.intermediate.ICode;
import wci.intermediate.ICodeNode;
import wci.intermediate.SymTab;
import wci.intermediate.SymTabStack;
import wci.message.Message;
import wci.message.MessageType;

public class Executor extends Backend {
    protected static int executionCount;
    protected static RuntimeErrorHandler errorHandler;

    static {
        executionCount = 0;
        errorHandler = new RuntimeErrorHandler();
    }

    public Executor() {
    }

    public Executor(Executor parent) {
        super();
    }

    public RuntimeErrorHandler getErrorHandler() {
        return errorHandler;
    }

    @Override
    public void process(ICode iCode, SymTabStack symTabStack) throws Exception {

        this.symTabStack = symTabStack;
        this.iCode = iCode;


        long startTime = System.currentTimeMillis();

        //运行
        ICodeNode rootNode = iCode.getRoot();
        StatementExecutor statementExecutor = new StatementExecutor(this);
        statementExecutor.execute(rootNode);

        float elapsedTime = (System.currentTimeMillis() - startTime) / 1000f;
        int executionCount = 0;
        int runtimeErrors = 0;

        sendMessage(
                new Message(
                        MessageType.INTERPRETER_SUMMARY,
                        new Number[]{
                                executionCount,
                                runtimeErrors,
                                elapsedTime
                        }
                )
        );
    }
}
