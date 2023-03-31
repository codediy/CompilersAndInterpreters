package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.Definition;
import wci.intermediate.SymTabEntry;
import wci.intermediate.TypeFactory;
import wci.intermediate.TypeSpec;
import wci.intermediate.symtabimpl.DefinitionImpl;
import wci.intermediate.symtabimpl.Predefined;
import wci.intermediate.symtabimpl.SymTabKeyImpl;

import java.util.EnumSet;

import static wci.frontend.pascal.PascalTokenType.*;

public class ConstantDefinitionsParser extends DeclarationsParser {
    public ConstantDefinitionsParser(PascalParserTD parent) {
        super(parent);
    }


    /**
     * id start
     * type var procedure function id begin
     */
    private static final EnumSet<PascalTokenType> IDENTIFIER_SET =
            DeclarationsParser.TYPE_START_SET.clone();

    static {
        IDENTIFIER_SET.add(IDENTIFIER);
    }

    static final EnumSet<PascalTokenType> CONSTANT_START_SET =
            EnumSet.of(IDENTIFIER, INTEGER, REAL, PLUS, MINUS, STRING, SEMICOLON);

    private static final EnumSet<PascalTokenType> EQUALS_SET =
            CONSTANT_START_SET.clone();

    static {
        EQUALS_SET.add(EQUALS);
        EQUALS_SET.add(SEMICOLON);
    }

    private static final EnumSet<PascalTokenType> NEXT_START_SET =
            DeclarationsParser.TYPE_START_SET.clone();

    static {
        NEXT_START_SET.add(SEMICOLON);
        NEXT_START_SET.add(IDENTIFIER);
    }

    /**
     * {id = value ;}+
     *
     * @param token
     * @throws Exception
     */
    @Override
    public void parse(Token token) throws Exception {
        token = synchronize(IDENTIFIER_SET);

        while (token.getType() == IDENTIFIER) {
            String name = token.getText().toLowerCase();
            SymTabEntry constantId = symTabStack.lookupLocal(name);

            if (constantId == null) {
                constantId = symTabStack.enterLocal(name);
                constantId.appendLineNumber(token.getLineNum());
            } else {
                errorHandler.flag(
                        token,
                        PascalErrorCode.IDENTIFIER_REDEFINED,
                        this
                );
                constantId = null;
            }

            token = nextToken();

            // =
            token = synchronize(EQUALS_SET);
            if (token.getType() == EQUALS) {
                token = nextToken();
            } else {
                errorHandler.flag(
                        token,
                        PascalErrorCode.MISSING_EQUALS,
                        this
                );
            }

            // value
            Token constantToken = token;
            Object value = parseConstant(token);

            // constant type
            if (constantId != null) {
                constantId.setDefinition(DefinitionImpl.CONSTANT);
                constantId.setAttribute(SymTabKeyImpl.CONSTANT_VALUE, value);

                TypeSpec constantType =
                        constantToken.getType() == IDENTIFIER
                                ? getConstantType(constantToken)
                                : getConstantType(value);
                //记录常量的类型
                constantId.setTypeSpec(constantType);
            }

            // ;
            token = currentToken();
            TokenType tokenType = token.getType();

            if (tokenType == SEMICOLON) {
                while (token.getType() == SEMICOLON) {
                    token = nextToken();
                }
            } else if (NEXT_START_SET.contains(tokenType)) {
                errorHandler.flag(
                        token,
                        PascalErrorCode.MISSING_SEMICOLON,
                        this
                );
            }
            token = synchronize(IDENTIFIER_SET);
        }
    }

    /**
     * sign ? id/integer/real/string
     *
     * @param token
     * @return
     * @throws Exception
     */
    protected Object parseConstant(Token token) throws Exception {
        TokenType sign = null;

        token = synchronize(CONSTANT_START_SET);
        TokenType tokenType = token.getType();

        //符号
        if ((tokenType == PLUS) || (tokenType == MINUS)) {
            sign = tokenType;
            token = nextToken();
        }

        switch ((PascalTokenType) token.getType()) {
            case IDENTIFIER: {
                return parseIdentifierConstant(token, sign);
            }
            case INTEGER: {
                Integer value = (Integer) token.getValue();
                nextToken();
                return sign == MINUS ? -value : value;
            }
            case REAL: {
                Float value = (Float) token.getValue();
                nextToken();
                return sign == MINUS ? -value : value;
            }
            case STRING: {
                if (sign != null) {
                    errorHandler.flag(
                            token,
                            PascalErrorCode.INVALID_CONSTANT,
                            this
                    );
                }
                nextToken();
                return (String) token.getValue();
            }
            default: {
                errorHandler.flag(token,
                        PascalErrorCode.INVALID_CONSTANT,
                        this);
                return null;
            }
        }
    }

    protected Object parseIdentifierConstant(Token token, TokenType sign)
            throws Exception {
        //name
        String name = token.getText().toLowerCase();
        SymTabEntry id = symTabStack.lookup(name);

        nextToken();

        if (id == null) {
            errorHandler.flag(
                    token,
                    PascalErrorCode.IDENTIFIER_UNDEFINED,
                    this
            );
            return null;
        }
        // id的类型
        Definition definition = id.getDefinition();
        if (definition == DefinitionImpl.CONSTANT) {
            Object value = id.getAttribute(SymTabKeyImpl.CONSTANT_VALUE);
            id.appendLineNumber(token.getLineNum());

            if (value instanceof Integer) {
                return sign == MINUS ? -((Integer) value) : value;
            } else if (value instanceof Float) {
                return sign == MINUS ? -((Float) value) : value;
            } else if (value instanceof String) {
                if (sign != null) {
                    errorHandler.flag(
                            token,
                            PascalErrorCode.INVALID_CONSTANT,
                            this
                    );
                    return value;
                }
            } else {
                return null;
            }
        } else if (definition == DefinitionImpl.ENUMERATION_CONSTANT) {
            Object value = id.getAttribute(SymTabKeyImpl.CONSTANT_VALUE);
            id.appendLineNumber(token.getLineNum());

            if (sign != null) {
                errorHandler.flag(token, PascalErrorCode.INVALID_CONSTANT, this);
            }
            return value;
        } else if (definition == null) {
            errorHandler.flag(token, PascalErrorCode.NOT_CONSTANT_IDENTIFIER, this);
            return null;
        } else {
            errorHandler.flag(token, PascalErrorCode.INVALID_CONSTANT, this);
            return null;
        }
        return null;
    }

    protected TypeSpec getConstantType(Object value) {
        TypeSpec constantType = null;
        if (value instanceof Integer) {
            constantType = Predefined.integerType;
        } else if (value instanceof Float) {
            constantType = Predefined.realType;
        } else if (value instanceof String) {
            if (((String) value).length() == 1) {
                constantType = Predefined.charType;
            } else {
                constantType = TypeFactory.createStringType((String) value);
            }
        }

        return constantType;
    }

    protected TypeSpec getConstantType(Token identifier) {
        String name = identifier.getText().toLowerCase();
        SymTabEntry id = symTabStack.lookup(name);

        if (id == null) {
            return null;
        }

        Definition definition = id.getDefinition();
        if ((definition == DefinitionImpl.CONSTANT)
                || (definition == DefinitionImpl.ENUMERATION_CONSTANT)) {
            return id.getTypeSpec();
        } else {
            return null;
        }
    }

}
