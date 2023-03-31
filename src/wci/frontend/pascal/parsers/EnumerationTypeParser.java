package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.SymTabEntry;
import wci.intermediate.TypeFactory;
import wci.intermediate.TypeSpec;
import wci.intermediate.symtabimpl.DefinitionImpl;
import wci.intermediate.symtabimpl.SymTabKeyImpl;
import wci.intermediate.typeimpl.TypeFormImpl;
import wci.intermediate.typeimpl.TypeKeyImpl;

import java.util.ArrayList;
import java.util.EnumSet;

import static wci.frontend.pascal.PascalTokenType.*;

public class EnumerationTypeParser extends TypeSpecificationParser {
    public EnumerationTypeParser(PascalParserTD parent) {
        super(parent);
    }

    private static final EnumSet<PascalTokenType> ENUM_CONSTANT_START_SET =
            EnumSet.of(IDENTIFIER, COMMA);

    private static final EnumSet<PascalTokenType> ENUM_DEFINITION_FOLLOW_SET =
            EnumSet.of(RIGHT_PAREN, SEMICOLON);

    static {
        ENUM_DEFINITION_FOLLOW_SET.addAll(DeclarationsParser.VAR_START_SET);
    }

    @Override
    public TypeSpec parse(Token token) throws Exception {
        TypeSpec enumerationType = TypeFactory.createType(TypeFormImpl.ENUMERATION);
        int value = -1;
        ArrayList<SymTabEntry> constants = new ArrayList<SymTabEntry>();

        token = nextToken();

        do {
            token = synchronize(ENUM_CONSTANT_START_SET);
            //收集枚举的值
            parseEnumerationIdentifier(token, ++value, enumerationType, constants);

            token = currentToken();
            TokenType tokenType = token.getType();

            if (tokenType == COMMA) {
                token = nextToken();
                if (ENUM_DEFINITION_FOLLOW_SET.contains(token.getType())) {
                    errorHandler.flag(
                            token,
                            PascalErrorCode.MISSING_IDENTIFIER,
                            this
                    );
                }
            } else if (ENUM_CONSTANT_START_SET.contains(tokenType)) {
                errorHandler.flag(
                        token,
                        PascalErrorCode.MISSING_COMMA,
                        this
                );
            }

        } while (!ENUM_DEFINITION_FOLLOW_SET.contains(token.getType()));

        if (token.getType() == RIGHT_PAREN) {
            token = nextToken();
        } else {
            errorHandler.flag(
                    token,
                    PascalErrorCode.MISSING_RIGHT_PAREN,
                    this
            );
        }

        enumerationType.setAttribute(TypeKeyImpl.ENUMERATION_CONSTANTS, constants);
        return enumerationType;
    }

    /**
     * 枚举中的id
     *
     * @param token
     * @param value
     * @param enumerationType
     * @param constants
     * @throws Exception
     */
    private void parseEnumerationIdentifier(
            Token token,
            int value,
            TypeSpec enumerationType,
            ArrayList<SymTabEntry> constants
    ) throws Exception {
        TokenType tokenType = token.getType();

        if (tokenType == IDENTIFIER) {
            String name = token.getText().toLowerCase();
            SymTabEntry constantId = symTabStack.lookupLocal(name);

            if (constantId != null) {
                errorHandler.flag(
                        token,
                        PascalErrorCode.IDENTIFIER_REDEFINED,
                        this
                );
            } else {
                constantId = symTabStack.enterLocal(name);

                constantId.setDefinition(DefinitionImpl.ENUMERATION_CONSTANT);
                constantId.setTypeSpec(enumerationType);
                constantId.setAttribute(SymTabKeyImpl.CONSTANT_VALUE, value);
                constantId.appendLineNumber(token.getLineNum());
                constants.add(constantId);
            }

            token = nextToken();
        } else {
            errorHandler.flag(token, PascalErrorCode.MISSING_IDENTIFIER, this);
        }
    }
}
