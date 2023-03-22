package wci.frontend.pascal.parsers;

import wci.frontend.EofToken;
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
import java.util.HashSet;

import static wci.frontend.pascal.PascalErrorCode.INVALID_CONSTANT;
import static wci.frontend.pascal.PascalTokenType.*;

public class CaseStatementParser extends StatementParser {
    public CaseStatementParser(PascalParserTD parent) {
        super(parent);
    }

    private static final EnumSet<PascalTokenType> CONSTANT_START_SET =
            EnumSet.of(IDENTIFIER, INTEGER, PLUS, MINUS, STRING);
    private static final EnumSet<PascalTokenType> OF_SET =
            CONSTANT_START_SET.clone();

    static {
        OF_SET.add(OF);
        OF_SET.addAll(StatementParser.STMT_FOLLOW_SET);
    }

    /**
     * @param token
     * @return
     * @throws Exception
     */
    @Override
    public ICodeNode parse(Token token) throws Exception {

        ICodeNode selectNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.SELECT);
        setLineNumber(selectNode, token);
        // case
        token = nextToken();
        // expression
        ExpressionParser expressionParser = new ExpressionParser(this);
        selectNode.addChild(expressionParser.parse(token));

        // OF
        token = synchronize(OF_SET);
        if (token.getType() == OF) {
            token = nextToken();
        } else {
            errorHandler.flag(token,
                    PascalErrorCode.MISSING_OF,
                    this);
        }

        HashSet<Object> constantSet = new HashSet<Object>();
        // { BRANCH: xx ;}+
        while (!(token instanceof EofToken) && (token.getType() != END)) {
            selectNode.addChild(parseBranch(token, constantSet));

            token = currentToken();
            TokenType tokenType = token.getType();

            if (tokenType == SEMICOLON) {
                // 单独一条Branch;
                token = nextToken();
            } else if (CONSTANT_START_SET.contains(tokenType)) {
                errorHandler.flag(token,
                        PascalErrorCode.MISSING_SEMICOLON,
                        this);
            }
        }

        // end
        if (token.getType() == END) {
            token = nextToken();
        } else {
            errorHandler.flag(token,
                    PascalErrorCode.MISSING_END,
                    this);
        }
        return selectNode;
    }

    /**
     * xx,yy:statementList
     *
     * @param token
     * @param constantSet
     * @return
     * @throws Exception
     */
    private ICodeNode parseBranch(Token token, HashSet<Object> constantSet)
            throws Exception {

        // xx
        ICodeNode branchNode = ICodeFactory.createICodeNode(
                ICodeNodeTypeImpl.SELECT_BRANCH
        );
        setLineNumber(branchNode, token);

        ICodeNode constantsNode = ICodeFactory.createICodeNode(
                ICodeNodeTypeImpl.SELECT_CONSTANTS
        );
        branchNode.addChild(constantsNode);
        // xx,xx
        parseConstantList(token, constantsNode, constantSet);

        // :
        token = currentToken();
        if (token.getType() == COLON) {
            token = nextToken();
        } else {
            errorHandler.flag(
                    token,
                    PascalErrorCode.MISSING_COLON,
                    this
            );
        }

        // statement
        StatementParser statementParser = new StatementParser(this);
        branchNode.addChild(statementParser.parse(token));

        return branchNode;
    }

    private static final EnumSet<PascalTokenType> COMMA_SET =
            CONSTANT_START_SET.clone();

    static {
        COMMA_SET.add(COMMA);
        COMMA_SET.add(COLON);
        COMMA_SET.addAll(StatementParser.STMT_START_SET);
        COMMA_SET.addAll(StatementParser.STMT_FOLLOW_SET);
    }

    private void parseConstantList(
            Token token,
            ICodeNode constantsNode,
            HashSet<Object> constantSet
    ) throws Exception {
        while (CONSTANT_START_SET.contains(token.getType())) {
            constantsNode.addChild(parseConstant(token, constantSet));

            token = synchronize(COMMA_SET);
            if (token.getType() == COMMA) {
                // 逗号分割
                token = nextToken();
            } else if (CONSTANT_START_SET.contains(token.getType())) {
                // 缺少逗号
                errorHandler.flag(
                        token,
                        PascalErrorCode.MISSING_COMMA,
                        this
                );
            }
        }
    }


    private ICodeNode parseConstant(
            Token token,
            HashSet<Object> constantSet
    ) throws Exception {
        TokenType sign = null;
        ICodeNode constantNode = null;

        token = synchronize(CONSTANT_START_SET);
        TokenType tokenType = token.getType();

        //正负符号
        if ((tokenType == PLUS) || (tokenType == MINUS)) {
            sign = tokenType;
            token = nextToken();
        }

        // constant
        switch ((PascalTokenType) token.getType()) {
            case IDENTIFIER: {
                constantNode = parseIdentifierConstant(token, sign);
                break;
            }
            case INTEGER: {
                constantNode = parseIntegerConstant(token.getText(), sign);
                break;
            }
            case STRING: {
                constantNode = parseCharacterConstant(
                        token,
                        (String) token.getValue(),
                        sign
                );
                break;
            }
            default: {
                errorHandler.flag(token,
                        INVALID_CONSTANT,
                        this);
                break;
            }
        }

        //重复检测
        if (constantNode != null) {
            Object value = constantNode.getAttribute(ICodeKeyImpl.VALUE);

            if (constantSet.contains(value)) {
                errorHandler.flag(
                        token,
                        PascalErrorCode.CASE_CONSTANT_REUSED,
                        this
                );
            } else {
                constantSet.add(value);
            }
        }

        nextToken();
        return constantNode;
    }

    /**
     * 忽略Id
     *
     * @param token
     * @param sign
     * @return
     * @throws Exception
     */
    private ICodeNode parseIdentifierConstant(Token token, TokenType sign)
            throws Exception {
        errorHandler.flag(token, INVALID_CONSTANT, this);
        return null;
    }

    private ICodeNode parseIntegerConstant(String value, TokenType sign) {
        ICodeNode constantNode = ICodeFactory.createICodeNode(
                ICodeNodeTypeImpl.INTEGER_CONSTANT
        );
        int intValue = Integer.parseInt(value);
        if (sign == MINUS) {
            intValue = -intValue;
        }
        constantNode.setAttribute(ICodeKeyImpl.VALUE, intValue);
        return constantNode;
    }

    private ICodeNode parseCharacterConstant(Token token, String value,
                                             TokenType sign) {
        ICodeNode constantNode = null;
        if (sign != null) {
            errorHandler.flag(
                    token,
                    INVALID_CONSTANT,
                    this
            );
        } else {
            if (value.length() == 1) {
                constantNode = ICodeFactory.createICodeNode(
                        ICodeNodeTypeImpl.STRING_CONSTANT
                );
                constantNode.setAttribute(ICodeKeyImpl.VALUE, value);
            } else {
                errorHandler.flag(token,
                        INVALID_CONSTANT,
                        this);
            }
        }
        return constantNode;
    }
}
