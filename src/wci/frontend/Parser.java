package wci.frontend;

import wci.intermediate.ICode;
import wci.intermediate.SymTab;
import wci.intermediate.SymTabFactory;
import wci.intermediate.SymTabStack;
import wci.message.Message;
import wci.message.MessageHandler;
import wci.message.MessageListener;
import wci.message.MessageProducer;

public abstract class Parser implements MessageProducer {
    protected static SymTabStack symTabStack;
    protected static MessageHandler messageHandler;

    static {
        symTabStack = SymTabFactory.createSymTabStack();
        messageHandler = new MessageHandler();
    }

    protected Scanner scanner;
    protected ICode iCode;

    protected Parser(Scanner scanner) {
        this.scanner = scanner;
        this.iCode = null;
    }

    public abstract void parse() throws Exception;

    public abstract int getErrorCount();

    public Token currentToken() {
        return scanner.currentToken();
    }

    public Token nextToken() throws Exception {
        return scanner.nextToken();
    }

    public ICode getICode() {
        return iCode;
    }

    public SymTabStack getSymTabStack() {
        return symTabStack;
    }

    @Override
    public void addMessageListener(MessageListener listener) {
        messageHandler.addListener(listener);
    }

    @Override
    public void removeMessageListener(MessageListener listener) {
        messageHandler.removeListener(listener);
    }

    @Override
    public void sendMessage(Message message) {
        messageHandler.sendMessage(message);
    }
}
