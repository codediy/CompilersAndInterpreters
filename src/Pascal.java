import wci.backend.Backend;
import wci.backend.BackendFactory;
import wci.frontend.FrontendFactory;
import wci.frontend.Parser;
import wci.frontend.Source;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.ICode;
import wci.intermediate.SymTab;
import wci.intermediate.SymTabStack;
import wci.message.Message;
import wci.message.MessageListener;
import wci.message.MessageType;
import wci.util.CrossReferencer;

import java.io.BufferedReader;
import java.io.FileReader;

public class Pascal {
    private Parser parser;
    private Source source;
    private ICode iCode;
    private SymTabStack symTabStack;
    private Backend backend;


    public Pascal(
            String operation,
            String filePath,
            String flags
    ) {
        try {
            boolean intermediate = flags.indexOf("i") > -1;
            boolean xref = flags.indexOf("x") > -1;

            source = new Source(new BufferedReader(new FileReader(filePath)));
            source.addMessageListener(new SourceMessageListener());

            parser = FrontendFactory.createParser("Pascal",
                    "top-down", source);
            parser.addMessageListener(new ParserMessageListener());

            backend = BackendFactory.createBackend(operation);
            backend.addMessageListener(new BackendMessageListener());

            parser.parse();
            source.close();

            iCode = parser.getICode();
            symTabStack = parser.getSymTabStack();

            //xref 符号表索引打印
            if (xref) {
                CrossReferencer crossReferencer = new CrossReferencer();
                crossReferencer.print(symTabStack);
            }

            backend.process(iCode, symTabStack);
        } catch (Exception ex) {
            System.out.println("***** Internal translator error.****");
            ex.printStackTrace();
        }
    }

    private static final String FLAGS = "[-ix]";
    private static final String USAGE =
            "Usage:Pascal execute|compile " + FLAGS + "<source file path>";

    public static void main(String args[]) {
        try {
            String operation = args[0];
            if (!(operation.equalsIgnoreCase("compile")
                    || operation.equalsIgnoreCase("execute")
            )) {
                throw new Exception("no operation");
            }
            int i = 1;
            String flags = "";
            while (i < args.length && args[i].charAt(0) == '-') {
                flags += args[i].substring(1);
                i = i + 1;
            }
            if (i < args.length) {
                String path = args[i];
                new Pascal(operation, path, flags);
            } else {
                throw new Exception("path error");
            }
        } catch (Exception ex) {
            System.out.println(USAGE);
        }
    }

    private static final String SOURCE_LINE_FORMAT = "%03d %s";

    private class SourceMessageListener implements MessageListener {

        @Override
        public void messageReceived(Message message) {
            MessageType type = message.getType();
            Object body[] = (Object[]) message.getBody();
            switch (type) {
                case SOURCE_LINE: {
                    int lineNumber = (Integer) body[0];
                    String lineText = (String) body[1];
                    System.out.println(
                            String.format(
                                    SOURCE_LINE_FORMAT,
                                    lineNumber, lineText
                            )
                    );
                }
            }
        }
    }

    private static final String TOKEN_FORMAT =
            ">>> %-15s line=%03d,pos=%2d,text=\"%s\"";
    private static final String VALUE_FORMAT =
            ">>>                  value=%s";
    private static final String PARSER_SUMMARY_FORMAT =
            "\n%,20d source lines." +
                    "\n%,20d syntax error" +
                    "\n%,20.2f seconds total parsing time.\n";
    private static final int PREFIX_WIDTH = 5;

    private class ParserMessageListener implements MessageListener {

        @Override
        public void messageReceived(Message message) {
            MessageType type = message.getType();
            switch (type) {
                case TOKEN: {
                    Object body[] = (Object[]) message.getBody();
                    int line = (Integer) body[0];
                    int position = (Integer) body[1];
                    TokenType tokenType = (TokenType) body[2];
                    String tokenText = (String) body[3];
                    Object tokenValue = body[4];

                    System.out.println(
                            String.format(TOKEN_FORMAT,
                                    tokenType, line, position, tokenText)
                    );
                    if (tokenValue != null) {
                        if (tokenType == PascalTokenType.STRING) {
                            tokenValue = "\"" + tokenValue + "\"";
                        }
                        System.out.println(
                                String.format(VALUE_FORMAT, tokenValue)
                        );
                    }
                    break;
                }
                case SYNTAX_ERROR: {
                    Object body[] = (Object[]) message.getBody();
                    int lineNumber = (Integer) body[0];
                    int position = (Integer) body[1];
                    String tokenText = (String) body[2];
                    String errorMessage = (String) body[3];

                    int spaceCount = PREFIX_WIDTH + position;
                    StringBuilder flagBuffer = new StringBuilder();

                    for (int i = 1; i < spaceCount; ++i) {
                        flagBuffer.append(' ');
                    }
                    flagBuffer.append("^\n*** ").append(errorMessage);
                    if (tokenText != null) {
                        flagBuffer.append(" [at \"").append(tokenText)
                                .append("\"]");
                    }
                    System.out.println(flagBuffer.toString());
                    break;
                }
                case PARSER_SUMMARY: {
                    Number body[] = (Number[]) message.getBody();
                    int statementCount = (Integer) body[0];
                    int syntaxErrors = (Integer) body[1];
                    float elapsedTime = (Float) body[2];
                    System.out.printf(PARSER_SUMMARY_FORMAT,
                            statementCount, syntaxErrors, elapsedTime);
                    break;
                }
            }
        }
    }

    private static final String INTERPRETER_SUMMARY_FORMAT =
            "\n%,20d statements executed." +
                    "\n%,20d runtime errors." +
                    "\n%,20.2f  seconds total execution time.\n";
    private static final String COMPILER_SUMMARY_FORMAT =
            "\n%,20d instructions generated." +
                    "\n%,20.2f seconds total code generation time.\n";

    private class BackendMessageListener implements MessageListener {
        public void messageReceived(Message message) {
            MessageType type = message.getType();
            switch (type) {
                case INTERPRETER_SUMMARY: {
                    Number body[] = (Number[]) message.getBody();
                    int executionCount = (Integer) body[0];
                    int runtimeErrors = (Integer) body[1];
                    float elapsedTime = (Float) body[2];
                    System.out.printf(INTERPRETER_SUMMARY_FORMAT,
                            executionCount,
                            runtimeErrors,
                            elapsedTime);
                    break;
                }
                case COMPILER_SUMMARY: {
                    Number body[] = (Number[]) message.getBody();
                    int instructionCount = (Integer) body[0];
                    float elapsedTime = (Float) body[1];

                    System.out.printf(COMPILER_SUMMARY_FORMAT,
                            instructionCount, elapsedTime);
                    break;
                }
            }
        }
    }

}
