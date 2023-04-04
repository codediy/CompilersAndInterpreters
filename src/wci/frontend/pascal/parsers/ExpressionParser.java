package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.*;
import wci.intermediate.icodeimpl.ICodeKeyImpl;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;
import wci.intermediate.symtabimpl.DefinitionImpl;
import wci.intermediate.symtabimpl.Predefined;
import wci.intermediate.symtabimpl.SymTabKeyImpl;
import wci.intermediate.typeimpl.TypeChecker;

import java.util.EnumSet;
import java.util.HashMap;

import static wci.frontend.pascal.PascalTokenType.*;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.*;

public class ExpressionParser extends StatementParser {
    public ExpressionParser(PascalParserTD parent) {
        super(parent);
    }

    //表达式重启Token类型
    static final EnumSet<PascalTokenType> EXPR_START_SET =
            EnumSet.of(PLUS, MINUS, IDENTIFIER, INTEGER, REAL, STRING,
                    PascalTokenType.NOT, LEFT_PAREN);

    public ICodeNode parse(Token token) throws Exception {
        return parseExpression(token);
    }

    //1 Expression
    private static final EnumSet<PascalTokenType> REL_OPS =
            EnumSet.of(EQUALS, NOT_EQUALS, LESS_THAN, LESS_EQUALS,
                    GREATER_THAN, GREATER_EQUALS);

    private static final HashMap<PascalTokenType, ICodeNodeType> REL_OPS_MAP
            = new HashMap<PascalTokenType, ICodeNodeType>();

    static {
        REL_OPS_MAP.put(EQUALS, EQ);
        REL_OPS_MAP.put(NOT_EQUALS, NE);
        REL_OPS_MAP.put(LESS_THAN, LT);
        REL_OPS_MAP.put(LESS_EQUALS, LE);
        REL_OPS_MAP.put(GREATER_THAN, GT);
        REL_OPS_MAP.put(GREATER_EQUALS, GE);
    }

    /**
     * simpleExpression REL_OPS simpleExpression
     * <p>
     * REL_OPS               rootNode
     * simpleExpression ICodeNode
     * simpleExpression ICodeNode
     *
     * @param token
     * @return
     * @throws Exception
     */
    private ICodeNode parseExpression(Token token) throws Exception {
        ICodeNode rootNode = parseSimpleExpression(token);
        TypeSpec resultType = rootNode != null
                ? rootNode.getTypeSpec()
                : Predefined.undefinedType;

        token = currentToken();
        TokenType tokenType = token.getType();

        if (REL_OPS.contains(tokenType)) {
            ICodeNodeType nodeType = REL_OPS_MAP.get(tokenType);
            ICodeNode opNode = ICodeFactory.createICodeNode(nodeType);
            opNode.addChild(rootNode);

            token = nextToken();
            ICodeNode simExprNode = parseSimpleExpression(token);
            opNode.addChild(simExprNode);

            rootNode = opNode;

            TypeSpec simExprType = simExprNode != null
                    ? simExprNode.getTypeSpec()
                    : Predefined.undefinedType;
            if (TypeChecker.areComparisonCompatible(
                    resultType,
                    simExprType
            )) {
                resultType = Predefined.booleanType;
            } else {
                errorHandler.flag(
                        token,
                        PascalErrorCode.INCOMPATIBLE_TYPES,
                        this
                );
                resultType = Predefined.undefinedType;
            }
        }

        if (rootNode != null) {
            rootNode.setTypeSpec(resultType);
        }

        return rootNode;
    }

    //2 SimpleExpression
    private static final EnumSet<PascalTokenType> ADD_OPS =
            EnumSet.of(PLUS, MINUS, PascalTokenType.OR);

    private static final HashMap<PascalTokenType, ICodeNodeType> ADD_OPS_OPS_MAP
            = new HashMap<PascalTokenType, ICodeNodeType>();

    static {
        ADD_OPS_OPS_MAP.put(PLUS, ADD);
        ADD_OPS_OPS_MAP.put(MINUS, SUBTRACT);
        ADD_OPS_OPS_MAP.put(PascalTokenType.OR, ICodeNodeTypeImpl.OR);
    }

    /**
     * {+|-}? term (ADD_OP term }+
     *
     * @param token
     * @return
     * @throws Exception
     */
    private ICodeNode parseSimpleExpression(Token token) throws Exception {
        Token signToken = null;
        TokenType signTye = null;

        //正负符号
        TokenType tokenType = token.getType();
        if ((tokenType == PLUS) || (tokenType == MINUS)) {
            signTye = tokenType;
            signToken = token;
            token = nextToken();
        }

        ICodeNode rootNode = parseTerm(token);
        TypeSpec resultType = rootNode != null
                ? rootNode.getTypeSpec()
                : Predefined.undefinedType;
        //类型检查
        if ((signTye != null) && (!TypeChecker.isIntegerOrReal(resultType))) {
            errorHandler.flag(
                    signToken,
                    PascalErrorCode.INCOMPATIBLE_TYPES,
                    this
            );
        }

        if (signTye == MINUS) {
            ICodeNode negateNode = ICodeFactory.createICodeNode(NEGATE);
            negateNode.addChild(rootNode);
            negateNode.setTypeSpec(rootNode.getTypeSpec());
            rootNode = negateNode;
        }

        token = currentToken();
        tokenType = token.getType();

        while (ADD_OPS.contains(tokenType)) {
            TokenType operator = tokenType;
            ICodeNodeType nodeType = ADD_OPS_OPS_MAP.get(operator);
            ICodeNode opNode = ICodeFactory.createICodeNode(nodeType);
            opNode.addChild(rootNode);

            token = nextToken();

            ICodeNode termNode = parseTerm(token);
            opNode.addChild(termNode);
            TypeSpec termType = termNode != null
                    ? termNode.getTypeSpec()
                    : Predefined.undefinedType;

            rootNode = opNode;
            switch ((PascalTokenType) operator) {
                case PLUS:
                case MINUS: {
                    if (TypeChecker.areBothInteger(resultType, termType)) {
                        resultType = Predefined.integerType;
                    } else if (TypeChecker.isAtLeastOneReal(resultType, termType)) {
                        resultType = Predefined.realType;
                    } else {
                        errorHandler.flag(
                                token,
                                PascalErrorCode.INCOMPATIBLE_TYPES,
                                this
                        );
                    }
                    break;
                }
                case OR: {
                    if (TypeChecker.areBothBoolean(resultType, termType)) {
                        resultType = Predefined.booleanType;
                    } else {
                        errorHandler.flag(
                                token,
                                PascalErrorCode.INCOMPATIBLE_TYPES,
                                this
                        );
                    }
                    break;
                }
            }
            rootNode.setTypeSpec(resultType);
            token = currentToken();
            tokenType = token.getType();
        }

        return rootNode;
    }

    //3 Term
    private static final EnumSet<PascalTokenType> MULT_OPS =
            EnumSet.of(STAR, SLASH, DIV, PascalTokenType.MOD, PascalTokenType.AND);

    private static final HashMap<PascalTokenType, ICodeNodeType>
            MULT_OPS_OPS_MAP = new HashMap<PascalTokenType, ICodeNodeType>();

    static {
        MULT_OPS_OPS_MAP.put(STAR, MULTIPLY);
        MULT_OPS_OPS_MAP.put(SLASH, FLOAT_DIVIDE);
        MULT_OPS_OPS_MAP.put(DIV, INTEGER_DIVIDE);
        MULT_OPS_OPS_MAP.put(PascalTokenType.MOD, ICodeNodeTypeImpl.MOD);
        MULT_OPS_OPS_MAP.put(PascalTokenType.AND, ICodeNodeTypeImpl.AND);
    }

    ;

    /**
     * factor {MULT_OPS factor}+
     *
     * @param token
     * @return
     * @throws Exception
     */
    private ICodeNode parseTerm(Token token) throws Exception {
        ICodeNode rootNode = parseFactor(token);
        TypeSpec resultType = rootNode != null
                ? rootNode.getTypeSpec()
                : Predefined.undefinedType;


        token = currentToken();
        TokenType tokenType = token.getType();

        while (MULT_OPS.contains(tokenType)) {
            TokenType operator = tokenType;
            ICodeNodeType nodeType = MULT_OPS_OPS_MAP.get(operator);
            ICodeNode opNode = ICodeFactory.createICodeNode(nodeType);
            opNode.addChild(rootNode);

            token = nextToken();
            ICodeNode factorNode = parseFactor(token);
            opNode.addChild(factorNode);

            TypeSpec factorType = factorNode != null
                    ? factorNode.getTypeSpec()
                    : Predefined.undefinedType;

            rootNode = opNode;
            switch ((PascalTokenType) operator) {
                case STAR: {
                    if (TypeChecker.areBothInteger(resultType, factorType)) {
                        resultType = Predefined.integerType;
                    } else if (TypeChecker.isAtLeastOneReal(resultType, factorType)) {
                        resultType = Predefined.realType;
                    } else {
                        errorHandler.flag(
                                token,
                                PascalErrorCode.INCOMPATIBLE_TYPES,
                                this
                        );
                    }
                    break;
                }
                case SLASH: {
                    if (TypeChecker.areBothInteger(resultType, factorType)
                            || TypeChecker.isAtLeastOneReal(resultType, factorType)) {
                        resultType = Predefined.realType;
                    } else {
                        errorHandler.flag(
                                token,
                                PascalErrorCode.INCOMPATIBLE_TYPES,
                                this
                        );
                    }
                    break;
                }
                case DIV:
                case MOD: {
                    if (TypeChecker.areBothInteger(resultType, factorType)) {
                        resultType = Predefined.integerType;
                    } else {
                        errorHandler.flag(
                                token,
                                PascalErrorCode.INCOMPATIBLE_TYPES,
                                this
                        );
                    }
                    break;
                }
                case AND: {
                    if (TypeChecker.areBothBoolean(resultType, factorType)) {
                        resultType = Predefined.booleanType;
                    } else {
                        errorHandler.flag(
                                token,
                                PascalErrorCode.INCOMPATIBLE_TYPES,
                                this
                        );
                    }
                    break;
                }
            }
            rootNode.setTypeSpec(resultType);

            token = currentToken();
            tokenType = token.getType();
        }
        return rootNode;
    }

    //4 Factor
    private ICodeNode parseFactor(Token token) throws Exception {
        TokenType tokenType = token.getType();
        ICodeNode rootNode = null;

        switch ((PascalTokenType) tokenType) {
            case IDENTIFIER: {
//                String name = token.getText().toLowerCase();
//                SymTabEntry id = symTabStack.lookup(name);
//                if (id == null) {
//                    errorHandler.flag(
//                            token,
//                            PascalErrorCode.IDENTIFIER_UNDEFINED,
//                            this
//                    );
//                    id = symTabStack.enterLocal(name);
//                }
//
//                rootNode = ICodeFactory.createICodeNode(VARIABLE);
//                rootNode.setAttribute(ICodeKeyImpl.ID, id);
//
//                id.appendLineNumber(token.getLineNum());
//                token = nextToken();
                return parseIdentifier(token);
            }
            case INTEGER: {
                rootNode = ICodeFactory.createICodeNode(INTEGER_CONSTANT);
                rootNode.setAttribute(ICodeKeyImpl.VALUE, token.getValue());

                token = nextToken();
                rootNode.setTypeSpec(Predefined.integerType);
                break;
            }
            case REAL: {
                rootNode = ICodeFactory.createICodeNode(REAL_CONSTANT);
                rootNode.setAttribute(ICodeKeyImpl.VALUE, token.getValue());

                token = nextToken();

                rootNode.setTypeSpec(Predefined.realType);
                break;
            }
            case STRING: {
                String value = (String) token.getValue();

                rootNode = ICodeFactory.createICodeNode(STRING_CONSTANT);
                rootNode.setAttribute(ICodeKeyImpl.VALUE, value);

                TypeSpec resultType = value.length() == 1
                        ? Predefined.charType
                        : TypeFactory.createStringType(value);

                token = nextToken();
                rootNode.setTypeSpec(resultType);
                break;
            }
            case NOT: {
                token = nextToken();

                rootNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.NOT);

                ICodeNode factorNode = parseFactor(token);
                rootNode.addChild(factorNode);

                TypeSpec factorType = factorNode != null
                        ? factorNode.getTypeSpec()
                        : Predefined.undefinedType;
                if (!TypeChecker.isBoolean(factorType)) {
                    errorHandler.flag(
                            token,
                            PascalErrorCode.INCOMPATIBLE_TYPES,
                            this
                    );
                }
                rootNode.setTypeSpec(Predefined.booleanType);
                break;
            }
            case LEFT_PAREN: {
                token = nextToken();

                rootNode = parseExpression(token);
                TypeSpec resultType = rootNode != null
                        ? rootNode.getTypeSpec()
                        : Predefined.undefinedType;


                token = currentToken();
                if (token.getType() == RIGHT_PAREN) {
                    token = nextToken();
                } else {
                    errorHandler.flag(
                            token,
                            PascalErrorCode.MISSING_RIGHT_PAREN,
                            this
                    );
                }
                rootNode.setTypeSpec(resultType);
                break;
            }
            default: {
                errorHandler.flag(
                        token,
                        PascalErrorCode.UNEXPECTED_TOKEN,
                        this
                );
                break;
            }
        }
        return rootNode;
    }

    private ICodeNode parseIdentifier(Token token) throws Exception {
        ICodeNode rootNode = null;

        String name = token.getText().toLowerCase();
        SymTabEntry id = symTabStack.lookup(name);

        if (id == null) {
            errorHandler.flag(
                    token,
                    PascalErrorCode.IDENTIFIER_UNDEFINED,
                    this
            );
            id = symTabStack.enterLocal(name);
            id.setDefinition(DefinitionImpl.UNDEFINED);
            id.setTypeSpec(Predefined.undefinedType);
        }

        Definition defnCode = id.getDefinition();
        switch ((DefinitionImpl) defnCode) {
            case CONSTANT: {
                Object value = id.getAttribute(SymTabKeyImpl.CONSTANT_VALUE);
                TypeSpec type = id.getTypeSpec();

                if (value instanceof Integer) {
                    rootNode = ICodeFactory.createICodeNode(INTEGER_CONSTANT);
                    rootNode.setAttribute(ICodeKeyImpl.VALUE, value);
                } else if (value instanceof Float) {
                    rootNode = ICodeFactory.createICodeNode(REAL_CONSTANT);
                    rootNode.setAttribute(ICodeKeyImpl.VALUE, value);
                } else if (value instanceof String) {
                    rootNode = ICodeFactory.createICodeNode(STRING_CONSTANT);
                    rootNode.setAttribute(ICodeKeyImpl.VALUE, value);
                }

                id.appendLineNumber(token.getLineNumber());
                token = nextToken();

                if (rootNode != null) {
                    rootNode.setTypeSpec(type);
                }
                break;
            }
            case ENUMERATION_CONSTANT: {
                Object value = id.getAttribute(SymTabKeyImpl.CONSTANT_VALUE);
                TypeSpec type = id.getTypeSpec();

                rootNode = ICodeFactory.createICodeNode(INTEGER_CONSTANT);
                rootNode.setAttribute(ICodeKeyImpl.VALUE, value);

                id.appendLineNumber(token.getLineNumber());
                token = nextToken();

                rootNode.setTypeSpec(type);
                break;
            }

            default: {
                VariableParser variableParser = new VariableParser(this);
                rootNode = variableParser.parse(token, id);
                break;
            }
        }
        return rootNode;
    }
}
