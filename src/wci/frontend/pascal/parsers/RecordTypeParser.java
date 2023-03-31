package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.TypeFactory;
import wci.intermediate.TypeSpec;
import wci.intermediate.symtabimpl.DefinitionImpl;
import wci.intermediate.typeimpl.TypeFormImpl;
import wci.intermediate.typeimpl.TypeKeyImpl;

import java.util.EnumSet;

import static wci.frontend.pascal.PascalTokenType.*;

public class RecordTypeParser extends TypeSpecificationParser {
    public RecordTypeParser(PascalParserTD parent) {
        super(parent);
    }

    private static final EnumSet<PascalTokenType> END_SET =
            DeclarationsParser.VAR_START_SET.clone();

    static {
        END_SET.add(END);
        END_SET.add(SEMICOLON);
    }


    @Override
    public TypeSpec parse(Token token) throws Exception {
        TypeSpec recordType = TypeFactory.createType(TypeFormImpl.RECORD);
        token = nextToken();

        //作用域
        recordType.setAttribute(TypeKeyImpl.RECORD_SYMTAB, symTabStack.push());

        //field解析
        VariableDeclarationsParser variableDeclarationsParser =
                new VariableDeclarationsParser(this);
        variableDeclarationsParser.setDefinition(DefinitionImpl.FIELD);
        variableDeclarationsParser.parse(token);

        symTabStack.pop();

        token = synchronize(END_SET);
        if (token.getType() == END) {
            token = nextToken();
        } else {
            errorHandler.flag(
                    token,
                    PascalErrorCode.MISSING_END,
                    this
            );
        }
        return recordType;
    }
}
