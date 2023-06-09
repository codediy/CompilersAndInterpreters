package wci.frontend.pascal;

import wci.frontend.*;
import wci.frontend.pascal.parsers.BlockParser;
import wci.frontend.pascal.parsers.StatementParser;
import wci.frontend.pascal.tokens.PascalErrorToken;
import wci.intermediate.*;
import wci.intermediate.symtabimpl.DefinitionImpl;
import wci.intermediate.symtabimpl.Predefined;
import wci.intermediate.symtabimpl.SymTabKeyImpl;
import wci.message.Message;
import wci.message.MessageType;

import java.io.IOException;
import java.util.EnumSet;

public class PascalParserTD extends Parser {
    protected static PascalErrorHandler errorHandler = new PascalErrorHandler();

    private SymTabEntry routineId;


    public PascalParserTD(Scanner scanner) {
        super(scanner);
    }

    public PascalParserTD(PascalParserTD parent) {
        super(parent.getScanner());
    }

    public SymTabEntry getRoutineId() {
        return routineId;
    }

    @Override
    public void parse() throws Exception {
        Token token;
        long startTime = System.currentTimeMillis();

        iCode = ICodeFactory.createICode();

        //初始化 默认符号
        Predefined.initialize(symTabStack);

        routineId = symTabStack.enterLocal("DummyProgramName".toLowerCase());
        routineId.setDefinition(DefinitionImpl.PROGRAM);
        symTabStack.setProgramId(routineId);

        routineId.setAttribute(SymTabKeyImpl.ROUTINE_SYMTAB, symTabStack.push());
        routineId.setAttribute(SymTabKeyImpl.ROUTINE_ICODE, iCode);

        BlockParser blockParser = new BlockParser(this);

        try {
//            while (!((token = nextToken()) instanceof EofToken)) {
//
//                TokenType tokenType = token.getType();

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

            //symTab
//                if (tokenType == PascalTokenType.IDENTIFIER) {
//                    String name = token.getText().toLowerCase();
//
//                    SymTabEntry entry = symTabStack.lookup(name);
//                    if (entry == null) {
//                        entry = symTabStack.enterLocal(name);
//                    }
//
//                    entry.appendLineNumber(token.getLineNum());
//
//                } else if (tokenType == PascalTokenType.ERROR) {
//                    errorHandler.flag(token, (PascalErrorCode) token.getValue(),
//                            this);
//                }

//                ICodeNode rootNode = null;
//                if (token.getType() == PascalTokenType.BEGIN) {
//                    StatementParser statementParser = new StatementParser(this);
//                    rootNode = statementParser.parse(token);
//                    token = currentToken();
//                } else {
//                    errorHandler.flag(
//                            token,
//                            PascalErrorCode.UNEXPECTED_TOKEN,
//                            this
//                    );
//                }
//
//                if (token.getType() != PascalTokenType.DOT) {
//                    errorHandler.flag(token,
//                            PascalErrorCode.MISSING_PERIOD,
//                            this);
//                }
//
//                token = currentToken();
//                if (rootNode != null) {
//                    iCode.setRoot(rootNode);
//                }
//            }
            //Program
            token = nextToken();

            //Block解析开始
            ICodeNode rootNode = blockParser.parse(token, routineId);
            iCode.setRoot(rootNode);
            symTabStack.pop();

            token = currentToken();
            if (token.getType() != PascalTokenType.DOT) {
                errorHandler.flag(
                        token,
                        PascalErrorCode.MISSING_PERIOD,
                        this
                );
            }
            token = currentToken();

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


    public Token synchronize(EnumSet syncSet) throws Exception {
        Token token = currentToken();
        if (!syncSet.contains(token.getType())) {
            errorHandler.flag(
                    token,
                    PascalErrorCode.UNEXPECTED_TOKEN,
                    this
            );
            do {
                token = nextToken();
            } while (!(token instanceof EofToken) && !syncSet.contains(token.getType()));
        }
        return token;
    }

}
