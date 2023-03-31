package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.TypeSpec;

import java.util.EnumSet;

import static wci.frontend.pascal.PascalTokenType.*;

public class TypeSpecificationParser extends PascalParserTD {
    public TypeSpecificationParser(PascalParserTD parent) {
        super(parent);
    }


    static final EnumSet<PascalTokenType> TYPE_START_SET =
            SimpleTypeParser.SIMPLE_TYPE_START_SET.clone();

    static {
        TYPE_START_SET.add(PascalTokenType.ARRAY);
        TYPE_START_SET.add(PascalTokenType.RECORD);
        TYPE_START_SET.add(SEMICOLON);
    }


    public TypeSpec parse(Token token)
            throws Exception {
        token = synchronize(TYPE_START_SET);

        switch ((PascalTokenType) token.getType()) {
            case ARRAY: {
                ArrayTypeParser arrayTypeParser = new ArrayTypeParser(this);
                return arrayTypeParser.parse(token);
            }
            case RECORD: {
                RecordTypeParser recordTypeParser = new RecordTypeParser(this);
                return recordTypeParser.parse(token);
            }
            default: {
                SimpleTypeParser simpleTypeParser = new SimpleTypeParser(this);
                return simpleTypeParser.parse(token);
            }
        }
    }
}
