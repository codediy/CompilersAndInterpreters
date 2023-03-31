package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.Definition;
import wci.intermediate.SymTabEntry;
import wci.intermediate.TypeSpec;

import java.util.ArrayList;
import java.util.EnumSet;

import static wci.frontend.pascal.PascalTokenType.*;

public class VariableDeclarationsParser extends DeclarationsParser {
    private Definition definition;

    public VariableDeclarationsParser(PascalParserTD parent) {
        super(parent);
    }

    protected void setDefinition(Definition definition) {
        this.definition = definition;
    }

    static final EnumSet<PascalTokenType> IDENTIFIER_SET =
            DeclarationsParser.VAR_START_SET.clone();

    static {
        IDENTIFIER_SET.add(IDENTIFIER);
        IDENTIFIER_SET.add(END);
        IDENTIFIER_SET.add(SEMICOLON);
    }

    static final EnumSet<PascalTokenType> NEXT_START_SET =
            DeclarationsParser.ROUTINE_START_SET.clone();

    static {
        NEXT_START_SET.add(IDENTIFIER);
        NEXT_START_SET.add(SEMICOLON);
    }

    /**
     * {id,id1,id..:type}+
     *
     * @param token
     * @throws Exception
     */
    @Override
    public void parse(Token token) throws Exception {
        token = synchronize(IDENTIFIER_SET);

        while (token.getType() == IDENTIFIER) {
            parseIdentifierSublist(token);

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
            //再次读取token
            token = synchronize(IDENTIFIER_SET);
        }
    }

    static final EnumSet<PascalTokenType> IDENTIFIER_START_SET =
            EnumSet.of(IDENTIFIER, COMMA);
    private static final EnumSet<PascalTokenType> IDENTIFIER_FOLLOW_SET =
            EnumSet.of(COLON, SEMICOLON);

    static {
        IDENTIFIER_FOLLOW_SET.addAll(DeclarationsParser.VAR_START_SET);
    }

    private static final EnumSet<PascalTokenType> COMMA_SET =
            EnumSet.of(COMMA, COLON, IDENTIFIER, SEMICOLON);

    protected ArrayList<SymTabEntry> parseIdentifierSublist(Token token)
            throws Exception {
        ArrayList<SymTabEntry> sublist = new ArrayList<SymTabEntry>();

        do {
            token = synchronize(IDENTIFIER_START_SET);
            SymTabEntry id = parseIdentifier(token);

            if (id != null) {
                sublist.add(id);
            }

            token = synchronize(COMMA_SET);
            TokenType tokenType = token.getType();
            ;

            if (tokenType == COMMA) {
                token = nextToken();
                if (IDENTIFIER_FOLLOW_SET.contains(token.getType())) {
                    errorHandler.flag(
                            token,
                            PascalErrorCode.MISSING_IDENTIFIER,
                            this
                    );
                }
            } else if (IDENTIFIER_START_SET.contains(tokenType)) {
                errorHandler.flag(
                        token,
                        PascalErrorCode.MISSING_COMMA,
                        this
                );
            }
        } while (!IDENTIFIER_FOLLOW_SET.contains(token.getType()));


        TypeSpec type = parseTypeSPec(token);
        //设置变量的type信息
        for (SymTabEntry variableId : sublist) {
            variableId.setTypeSpec(type);
        }
        return sublist;
    }

    private SymTabEntry parseIdentifier(Token token)
            throws Exception {
        SymTabEntry id = null;
        if (token.getType() == IDENTIFIER) {
            String name = token.getText().toLowerCase();
            id = symTabStack.lookupLocal(name);

            if (id == null) {
                id = symTabStack.enterLocal(name);
                id.setDefinition(definition);
                id.appendLineNumber(token.getLineNum());
            } else {
                errorHandler.flag(
                        token,
                        PascalErrorCode.IDENTIFIER_REDEFINED,
                        this
                );
            }

            token = nextToken();
        } else {
            errorHandler.flag(
                    token,
                    PascalErrorCode.MISSING_IDENTIFIER,
                    this
            );
        }
        return id;
    }

    private static final EnumSet<PascalTokenType> COLON_SET =
            EnumSet.of(COLON, SEMICOLON);

    protected TypeSpec parseTypeSPec(Token token)
            throws Exception {
        token = synchronize(COLON_SET);
        if (token.getType() == COLON) {
            token = nextToken();
        } else {
            errorHandler.flag(
                    token,
                    PascalErrorCode.MISSING_COLON,
                    this
            );
        }

        TypeSpecificationParser typeSpecificationParser =
                new TypeSpecificationParser(this);
        TypeSpec type = typeSpecificationParser.parse(token);

        return type;
    }
}
