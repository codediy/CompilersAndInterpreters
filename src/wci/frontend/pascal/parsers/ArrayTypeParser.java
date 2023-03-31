package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.SymTabEntry;
import wci.intermediate.TypeFactory;
import wci.intermediate.TypeForm;
import wci.intermediate.TypeSpec;
import wci.intermediate.typeimpl.TypeFormImpl;
import wci.intermediate.typeimpl.TypeKeyImpl;

import java.util.ArrayList;
import java.util.EnumSet;

import static wci.frontend.pascal.PascalTokenType.*;

public class ArrayTypeParser extends TypeSpecificationParser {
    public ArrayTypeParser(PascalParserTD parent) {
        super(parent);
    }

    /**
     *
     */
    private static final EnumSet<PascalTokenType> LEFT_BRACKET_SET =
            SimpleTypeParser.SIMPLE_TYPE_START_SET.clone();

    static {
        LEFT_BRACKET_SET.add(LEFT_BRACKET);
        LEFT_BRACKET_SET.add(RIGHT_BRACKET);
    }

    private static final EnumSet<PascalTokenType> RIGHT_BRACKET_SET =
            EnumSet.of(RIGHT_BRACKET, OF, SEMICOLON);
    private static final EnumSet<PascalTokenType> OF_SET =
            TypeSpecificationParser.TYPE_START_SET.clone();

    static {
        OF_SET.add(OF);
        OF_SET.add(SEMICOLON);
    }

    /**
     * [维度1,维度2,...] OF type
     * @param token
     * @return
     * @throws Exception
     */
    @Override
    public TypeSpec parse(Token token) throws Exception {
        TypeSpec arrayType = TypeFactory.createType(TypeFormImpl.ARRAY);
        token = nextToken();

        // [
        token = synchronize(LEFT_BRACKET_SET);
        if (token.getType() != LEFT_BRACKET) {
            errorHandler.flag(
                    token,
                    PascalErrorCode.MISSING_LEFT_BRACKET,
                    this
            );
        }
        // [,,,]中间的
        TypeSpec elementType = parseIndexTypeList(token, arrayType);

        //]
        token = synchronize(RIGHT_BRACKET_SET);
        if (token.getType() == RIGHT_BRACKET) {
            token = nextToken();
        } else {
            errorHandler.flag(
                    token,
                    PascalErrorCode.MISSING_RIGHT_PAREN,
                    this
            );
        }

        //OF
        token = synchronize(OF_SET);
        if (token.getType() == PascalTokenType.OF) {
            token = nextToken();
        } else {
            errorHandler.flag(token, PascalErrorCode.MISSING_OF, this);
        }

        // element
        elementType.setAttribute(TypeKeyImpl.ARRAY_ELEMENT_TYPE, parseElementType(token));

        return arrayType;
    }

    private static final EnumSet<PascalTokenType> INDEX_START_SET =
            SimpleTypeParser.SIMPLE_TYPE_START_SET.clone();

    static {
        INDEX_START_SET.add(COMMA);
    }

    private static final EnumSet<PascalTokenType> INDEX_END_SET =
            EnumSet.of(RIGHT_BRACKET, OF, SEMICOLON);

    private static final EnumSet<PascalTokenType> INDEX_FOLLOW_SET =
            INDEX_START_SET.clone();

    static {
        INDEX_FOLLOW_SET.addAll(INDEX_END_SET);
    }

    private TypeSpec parseIndexTypeList(
            Token token,
            TypeSpec arrayType
    ) throws Exception {
        TypeSpec elementType = arrayType;
        boolean anotherIndex = false;

        // [
        token = nextToken();

        do {
            anotherIndex = false;

            token = synchronize(INDEX_START_SET);
            //单个index解析
            parseIndexType(token, elementType);

            token = synchronize(INDEX_FOLLOW_SET);
            TokenType tokenType = token.getType();

            // , ] 是否结束类型
            if ((tokenType != COMMA) && (tokenType != RIGHT_BRACKET)) {
                if (INDEX_START_SET.contains(tokenType)) {
                    errorHandler.flag(
                            token,
                            PascalErrorCode.MISSING_COMMA,
                            this
                    );
                    anotherIndex = true;
                }
            } else if (tokenType == COMMA) {
                TypeSpec newElementType = TypeFactory.createType(TypeFormImpl.ARRAY);
                elementType.setAttribute(TypeKeyImpl.ARRAY_ELEMENT_TYPE, newElementType);
                elementType = newElementType;

                token = nextToken();
                anotherIndex = true;
            }

        } while (anotherIndex);
        return elementType;
    }

    /**
     * 数组类型的长度信息
     *
     * @param token
     * @param arrayType
     * @throws Exception
     */
    private void parseIndexType(
            Token token,
            TypeSpec arrayType
    ) throws Exception {
        SimpleTypeParser simpleTypeParser = new SimpleTypeParser(this);
        TypeSpec indexType = simpleTypeParser.parse(token);

        arrayType.setAttribute(TypeKeyImpl.ARRAY_INDEX_TYPE, indexType);

        if (indexType == null) {
            return;
        }

        TypeForm form = indexType.getForm();
        int count = 0;

        if (form == TypeFormImpl.SUBRANGE) {
            Integer minValue = (Integer) indexType.getAttribute(TypeKeyImpl.SUBRANGE_MIN_VALUE);
            Integer maxValue = (Integer) indexType.getAttribute(TypeKeyImpl.SUBRANGE_MAX_VALUE);
            if ((minValue != null) && (maxValue != null)) {
                count = maxValue - minValue + 1;
            }
        } else if (form == TypeFormImpl.ENUMERATION) {
            ArrayList<SymTabEntry> constants = (ArrayList<SymTabEntry>) indexType.getAttribute(
                    TypeKeyImpl.ENUMERATION_CONSTANTS
            );
            count = constants.size();
        } else {
            errorHandler.flag(token, PascalErrorCode.INVALID_INDEX_TYPE, this);
        }

        arrayType.setAttribute(TypeKeyImpl.ARRAY_ELEMENT_COUNT, count);
    }

    private TypeSpec parseElementType(Token token)
            throws Exception {
        TypeSpecificationParser typeSpecificationParser =
                new TypeSpecificationParser(this);
        return typeSpecificationParser.parse(token);
    }
}
