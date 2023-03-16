package wci.frontend.pascal;

import wci.frontend.*;
import wci.frontend.pascal.tokens.PascalErrorToken;
import wci.intermediate.SymTabEntry;
import wci.intermediate.SymTabStack;
import wci.message.Message;
import wci.message.MessageType;

import java.io.IOException;

public class PascalParserTD extends Parser {
    protected static PascalErrorHandler errorHandler = new PascalErrorHandler();

    public PascalParserTD(Scanner scanner) {
        super(scanner);
    }

    @Override
    public void parse() throws Exception {
        Token token;
        long startTime = System.currentTimeMillis();
        try {
            while (!((token = nextToken()) instanceof EofToken)) {

                TokenType tokenType = token.getType();


                //symTab
                if (tokenType == PascalTokenType.IDENTIFIER) {
                    String name = token.getText().toLowerCase();

                    SymTabEntry entry = symTabStack.lookup(name);
                    if (entry == null) {
                        entry = symTabStack.enterLocal(name);
                    }

                    entry.appendLineNumber(token.getLineNum());

                } else if (tokenType == PascalTokenType.ERROR) {
                    errorHandler.flag(token, (PascalErrorCode) token.getValue(),
                            this);
                }

                //scanner
//                if (tokenType != PascalTokenType.ERROR) {
//                    sendMessage(new Message(MessageType.TOKEN,
//                            new Object[]{
//                                    token.getLineNum(),
//                                    token.getPosition(),
//                                    tokenType,
//                                    token.getText(),
//                                    token.getValue()
//                            }));
//                } else {
//                    errorHandler.flag(token, (PascalErrorCode) token.getValue(),
//                            this);
//                }
            }


            float elapsedTime = (System.currentTimeMillis() - startTime) / 1000f;
            sendMessage(
                    new Message(
                            MessageType.PARSER_SUMMARY,
                            new Number[]{
                                    token.getLineNum(),
                                    getErrorCount(),
                                    elapsedTime
                            }
                    )
            );

        } catch (IOException ex) {
            errorHandler.abortTranslation(PascalErrorCode.IO_ERROR, this);
        }

    }

    @Override
    public int getErrorCount() {
        return errorHandler.getErrorCount();
    }
}
