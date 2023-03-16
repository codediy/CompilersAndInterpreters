package wci.backend;

import wci.intermediate.ICode;
import wci.intermediate.SymTab;
import wci.intermediate.SymTabStack;
import wci.message.Message;
import wci.message.MessageHandler;
import wci.message.MessageListener;
import wci.message.MessageProducer;

public abstract class Backend implements MessageProducer {
    protected static SymTabStack symTabStack;
    protected static MessageHandler messageHandler;

    static {
        messageHandler = new MessageHandler();
    }

    protected ICode iCode;

    public SymTabStack getSymTabStack() {
        return symTabStack;
    }

    public abstract void process(ICode iCode, SymTabStack symTabStack) throws Exception;

    public void sendMessage(Message message) {
        messageHandler.sendMessage(message);
    }

    public void addMessageListener(MessageListener listener) {
        messageHandler.addListener(listener);
    }

    public void removeMessageListener(MessageListener listener) {
        messageHandler.removeListener(listener);
    }
}
