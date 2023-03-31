package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.SymTabEntry;
import wci.intermediate.TypeSpec;
import wci.intermediate.symtabimpl.DefinitionImpl;

import java.util.EnumSet;

import static wci.frontend.pascal.PascalTokenType.*;

public class TypeDefinitionsParser extends DeclarationsParser {
    public TypeDefinitionsParser(PascalParserTD parent) {
        super(parent);
    }


    /**
     * var start
     * var procedure function begin id
     */
    private static final EnumSet<PascalTokenType> IDENTIFIER_SET =
            DeclarationsParser.VAR_START_SET.clone();

    static {
        IDENTIFIER_SET.add(IDENTIFIER);
    }

    private static final EnumSet<PascalTokenType> EQUALS_SET =
            ConstantDefinitionsParser.CONSTANT_START_SET.clone();

    static {
        EQUALS_SET.add(EQUALS);
        EQUALS_SET.add(SEMICOLON);
    }

    private static final EnumSet<PascalTokenType> FOLLOW_SET =
            EnumSet.of(SEMICOLON);

    private static final EnumSet<PascalTokenType> NEXT_START_SET =
            DeclarationsParser.VAR_START_SET.clone();

    static {
        NEXT_START_SET.add(SEMICOLON);
        NEXT_START_SET.add(IDENTIFIER);
    }

    /**
     * TYPE
     * {id:type;}+
     *
     * @param token
     * @throws Exception
     */
    @Override
    public void parse(Token token) throws Exception {

        token = synchronize(IDENTIFIER_SET);

        while (token.getType() == IDENTIFIER) {
            String name = token.getText().toLowerCase();
            SymTabEntry typeId = symTabStack.lookupLocal(name);

            if (typeId == null) {
                typeId = symTabStack.enterLocal(name);
                typeId.appendLineNumber(token.getLineNum());
            } else {
                errorHandler.flag(
                        token,
                        PascalErrorCode.IDENTIFIER_REDEFINED,
                        this
                );
                typeId = null;
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

            // Type Spec
            TypeSpecificationParser typeSpecificationParser =
                    new TypeSpecificationParser(this);
            TypeSpec type = typeSpecificationParser.parse(token);

            if (typeId != null) {
                typeId.setDefinition(DefinitionImpl.TYPE);
            }

            // typeSpec <=> SymTabEntry
            if ((type != null) && (typeId != null)) {
                if (type.getIdentifier() == null) {
                    type.setIdentifier(typeId);
                }
                typeId.setTypeSpec(type);
            } else {
                token = synchronize(FOLLOW_SET);
            }
            //;
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
}
