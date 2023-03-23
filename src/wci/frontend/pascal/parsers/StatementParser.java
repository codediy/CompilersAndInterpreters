package wci.frontend.pascal.parsers;

import wci.frontend.EofToken;
import wci.frontend.Scanner;
import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.icodeimpl.ICodeKeyImpl;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;

import java.util.EnumSet;

import static wci.frontend.pascal.PascalTokenType.*;

public class StatementParser extends PascalParserTD {
    private PascalErrorCode er;

    public StatementParser(PascalParserTD parent) {
        super(parent);
    }

    public ICodeNode parse(Token token) throws Exception {
        ICodeNode statementNode = null;


        switch ((PascalTokenType) token.getType()) {
            case BEGIN: {
                CompoundStatementParser compoundParser = new CompoundStatementParser(this);
                statementNode = compoundParser.parse(token);
                break;
            }

            case IDENTIFIER: {
                AssignmentStatementParser assignmentStatementParser = new AssignmentStatementParser(this);
                statementNode = assignmentStatementParser.parse(token);
                break;
            }

            // ch7 control parse
            case REPEAT: {
                RepeatStatementParser repeatStatementParser =
                        new RepeatStatementParser(this);
                statementNode = repeatStatementParser.parse(token);
                break;
            }
            case WHILE: {
                WhileStatementParser whileStatementParser =
                        new WhileStatementParser(this);
                statementNode = whileStatementParser.parse(token);
                break;
            }
            case FOR: {
                ForStatementParser forParser = new ForStatementParser(this);
                statementNode = forParser.parse(token);
                break;
            }
            case IF: {
                IfStatementParser ifParser = new IfStatementParser(this);
                statementNode = ifParser.parse(token);
                break;
            }
            case CASE: {
                CaseStatementParser caseParser = new CaseStatementParser(this);
                statementNode = caseParser.parse(token);
                break;
            }
            default: {
                statementNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.NO_OP);
                break;
            }
        }

        return statementNode;
    }

    protected void setLineNumber(ICodeNode node, Token token) {
        if (node != null) {
            node.setAttribute(ICodeKeyImpl.LINE, token.getLineNum());
        }
    }

    protected static final EnumSet<PascalTokenType> STMT_START_SET =
            EnumSet.of(BEGIN, CASE, FOR, PascalTokenType.IF, REPEAT, WHILE,
                    IDENTIFIER, SEMICOLON);
    protected static final EnumSet<PascalTokenType> STMT_FOLLOW_SET =
            EnumSet.of(SEMICOLON, END, ELSE, UNTIL, DOT);

    /**
     * {statement ;}+
     *
     * @param token
     * @param parentNode
     * @param terminator
     * @param errorCode
     * @throws Exception
     */
    protected void parseList(
            Token token, ICodeNode parentNode,
            PascalTokenType terminator,
            PascalErrorCode errorCode
    ) throws Exception {

        EnumSet<PascalTokenType> terminatorSet = STMT_START_SET.clone();
        terminatorSet.add(terminator);

        while (!(token instanceof EofToken) && (token.getType() != terminator)) {
            // statement
            ICodeNode statementNode = parse(token);
            parentNode.addChild(statementNode);

            // ;
            token = currentToken();
            TokenType tokenType = token.getType();

            if (tokenType == PascalTokenType.SEMICOLON) {
                token = nextToken();
            } else if (STMT_START_SET.contains(tokenType)) {
                errorHandler.flag(token,
                        PascalErrorCode.MISSING_SEMICOLON,
                        this
                );
            }
            token = synchronize(terminatorSet);


//            else if (tokenType == PascalTokenType.IDENTIFIER) {
//                errorHandler.flag(token,
//                        PascalErrorCode.MISSING_SEMICOLON,
//                        this);
//            }

//            else if (tokenType != terminator) {
//                errorHandler.flag(token, PascalErrorCode.UNEXPECTED_TOKEN, this);
//                token = nextToken();
//            }
        }

        if (token.getType() == terminator) {
            token = nextToken();
        } else {
            errorHandler.flag(token, errorCode, this);
        }


    }

}
