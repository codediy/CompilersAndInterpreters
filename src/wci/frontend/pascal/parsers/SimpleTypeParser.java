package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.Definition;
import wci.intermediate.SymTabEntry;
import wci.intermediate.TypeSpec;
import wci.intermediate.symtabimpl.DefinitionImpl;

import java.util.EnumSet;

import static wci.frontend.pascal.PascalTokenType.*;

public class SimpleTypeParser extends TypeSpecificationParser {
    public SimpleTypeParser(PascalParserTD parent) {
        super(parent);
    }

    static final EnumSet<PascalTokenType> SIMPLE_TYPE_START_SET =
            ConstantDefinitionsParser.CONSTANT_START_SET.clone();

    static {
        SIMPLE_TYPE_START_SET.add(LEFT_PAREN);
        SIMPLE_TYPE_START_SET.add(COMMA);
        SIMPLE_TYPE_START_SET.add(SEMICOLON);
    }

    /**
     * type identifier
     * enumeration
     * subrange
     *
     * @param token
     * @return
     * @throws Exception
     */
    @Override
    public TypeSpec parse(Token token) throws Exception {
        token = synchronize(SIMPLE_TYPE_START_SET);

        switch ((PascalTokenType) token.getType()) {
            case IDENTIFIER: {
                String name = token.getText().toLowerCase();
                SymTabEntry id = symTabStack.lookup(name);

                if (id != null) {
                    Definition definition = id.getDefinition();

                    if (definition == DefinitionImpl.TYPE) {
                        id.appendLineNumber(token.getLineNum());
                        token = nextToken();
                        return id.getTypeSpec();
                    } else if ((definition != DefinitionImpl.CONSTANT) &&
                            (definition != DefinitionImpl.ENUMERATION_CONSTANT)) {
                        errorHandler.flag(
                                token,
                                PascalErrorCode.NOT_TYPE_IDENTIFIER,
                                this
                        );
                        token = nextToken();
                        return null;
                    } else {
                        SubrangeTypeParser subrangeTypeParser = new SubrangeTypeParser(this);
                        return subrangeTypeParser.parse(token);
                    }
                } else {
                    errorHandler.flag(
                            token,
                            PascalErrorCode.IDENTIFIER_UNDEFINED,
                            this
                    );
                    token = nextToken();
                    return null;
                }
            }
            case LEFT_PAREN: {
                EnumerationTypeParser enumerationTypeParser =
                        new EnumerationTypeParser(this);
                return enumerationTypeParser.parse(token);
            }
            case COMMA: {

            }
            case SEMICOLON: {
                errorHandler.flag(token, PascalErrorCode.INVALID_TYPE, this);
                return null;
            }
            default: {
                SubrangeTypeParser subrangeTypeParser = new SubrangeTypeParser(this);
                return subrangeTypeParser.parse(token);
            }
        }
    }
}
