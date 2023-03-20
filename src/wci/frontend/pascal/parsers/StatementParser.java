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

        while (!(token instanceof EofToken) && (token.getType() != terminator)) {
            // statement
            ICodeNode statementNode = parse(token);
            parentNode.addChild(statementNode);

            // ;
            token = currentToken();
            TokenType tokenType = token.getType();

            if (tokenType == PascalTokenType.SEMICOLON) {
                token = nextToken();
            } else if (tokenType == PascalTokenType.IDENTIFIER) {
                errorHandler.flag(token,
                        PascalErrorCode.MISSING_SEMICOLON,
                        this);
            } else if (tokenType != terminator) {
                errorHandler.flag(token, PascalErrorCode.UNEXPECTED_TOKEN, this);
                token = nextToken();
            }
        }

        if (token.getType() == terminator) {
            token = nextToken();
        } else {
            errorHandler.flag(token, errorCode, this);
        }


    }

}
