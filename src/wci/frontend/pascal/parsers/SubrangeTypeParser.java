package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.TypeFactory;
import wci.intermediate.TypeSpec;
import wci.intermediate.symtabimpl.Predefined;
import wci.intermediate.typeimpl.TypeFormImpl;
import wci.intermediate.typeimpl.TypeKeyImpl;

import static wci.intermediate.typeimpl.TypeFormImpl.ENUMERATION;

public class SubrangeTypeParser extends TypeSpecificationParser {
    public SubrangeTypeParser(PascalParserTD parent) {
        super(parent);
    }

    /**
     * A..D
     *
     * @param token
     * @return
     * @throws Exception
     */
    @Override
    public TypeSpec parse(Token token) throws Exception {
        TypeSpec subrangeType = TypeFactory.createType(TypeFormImpl.SUBRANGE);

        Object minValue = null;
        Object maxValue = null;

        Token constantToken = token;

        ConstantDefinitionsParser constantDefinitionsParser =
                new ConstantDefinitionsParser(this);
        minValue = constantDefinitionsParser.parseConstant(token);


        TypeSpec minType = constantToken.getType() == PascalTokenType.IDENTIFIER
                ? constantDefinitionsParser.getConstantType(constantToken)
                : constantDefinitionsParser.getConstantType(minValue);

        //最小的值
        minValue = checkValueType(constantToken, minValue, minType);
        token = currentToken();
        Boolean sawDotDot = false;

        if (token.getType() == PascalTokenType.DOT_DOT) {
            token = nextToken();
            sawDotDot = true;
        }

        //
        TokenType tokenType = token.getType();
        if (ConstantDefinitionsParser.CONSTANT_START_SET.contains(tokenType)) {
            if (!sawDotDot) {
                errorHandler.flag(
                        token,
                        PascalErrorCode.MISSING_DOT_DOT,
                        this
                );
            }

            token = synchronize(ConstantDefinitionsParser.CONSTANT_START_SET);
            constantToken = token;

            maxValue = constantDefinitionsParser.parseConstant(token);
            TypeSpec maxType = constantToken.getType() == PascalTokenType.IDENTIFIER
                    ? constantDefinitionsParser.getConstantType(constantToken)
                    : constantDefinitionsParser.getConstantType(maxValue);
            maxValue = checkValueType(constantToken, maxValue, maxType);

            if ((minType == null) || (maxType == null)) {
                errorHandler.flag(
                        constantToken,
                        PascalErrorCode.INCOMPATIBLE_TYPES,
                        this
                );
            } else if (minType != maxType) {
                errorHandler.flag(
                        constantToken,
                        PascalErrorCode.INVALID_SUBRANGE_TYPE,
                        this
                );
            } else if ((minType != null) && (maxType != null) &&
                    ((Integer) minValue >= (Integer) maxValue)
            ) {
                errorHandler.flag(
                        constantToken,
                        PascalErrorCode.MIN_GT_MAX,
                        this
                );
            }
        } else {
            errorHandler.flag(
                    constantToken,
                    PascalErrorCode.INVALID_SUBRANGE_TYPE,
                    this
            );
        }
        //
        subrangeType.setAttribute(TypeKeyImpl.SUBRANGE_BASE_TYPE, minType);
        subrangeType.setAttribute(TypeKeyImpl.SUBRANGE_MIN_VALUE, minValue);
        subrangeType.setAttribute(TypeKeyImpl.SUBRANGE_MAX_VALUE, maxValue);

        return subrangeType;
    }

    private Object checkValueType(Token token, Object value, TypeSpec type) {
        if (type == null) {
            return value;
        }
        if (type == Predefined.integerType) {
            return value;
        } else if (type == Predefined.charType) {
            char ch = ((String) value).charAt(0);
            return Character.getNumericValue(ch);
        } else if (type.getForm() == ENUMERATION) {
            return value;
        } else {
            errorHandler.flag(token, PascalErrorCode.INVALID_SUBRANGE_TYPE, this);
            return value;
        }
    }
}
