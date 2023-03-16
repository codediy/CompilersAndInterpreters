package wci.frontend.pascal.tokens;

import wci.frontend.Source;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalToken;
import wci.frontend.pascal.PascalTokenType;

public class PascalNumberToken extends PascalToken {
    private static final int MAX_EXPONENT = 37;

    public PascalNumberToken(Source source) throws Exception {
        super(source);
    }

    @Override
    protected void extract() throws Exception {
        StringBuilder textBuffer = new StringBuilder();
        extractNumber(textBuffer);
        text = textBuffer.toString();
    }

    protected void extractNumber(StringBuilder textBuffer) throws Exception {
        String wholeDigits = null;
        String fractionDigits = null;
        String exponentDigits = null;
        char exponentSign = '+';
        boolean sawDotDot = false;
        char currentChar;

        //默认整数
        type = PascalTokenType.INTEGER;

        wholeDigits = unsignedIntegerDigits(textBuffer);
        if (type == PascalTokenType.ERROR) {
            return;
        }

        //小数部分
        currentChar = currentChar();
        if (currentChar == '.') {
            if (peekChar() == '.') {
                // ..
                sawDotDot = true;
            } else {
                type = PascalTokenType.REAL;
                textBuffer.append(currentChar);
                currentChar = nextChar();

                fractionDigits = unsignedIntegerDigits(textBuffer);
                if (type == PascalTokenType.ERROR) {
                    return;
                }
            }
        }

        //指数部分
        currentChar = currentChar();
        if (!sawDotDot && (
                (currentChar == 'E') || (currentChar == 'e')
        )) {
            type = PascalTokenType.REAL;
            textBuffer.append(currentChar);
            currentChar = nextChar();
            if ((currentChar == '+') || (currentChar == '-')) {
                textBuffer.append(currentChar);
                exponentSign = currentChar;
                currentChar = nextChar();
            }

            exponentDigits = unsignedIntegerDigits(textBuffer);
        }

        //生成数字
        if (type == PascalTokenType.INTEGER) {
            int integerValue = computeIntegerValue(wholeDigits);
            if (type != PascalTokenType.ERROR) {
                value = new Integer(integerValue);
            }
        } else if (type == PascalTokenType.REAL) {
            float floatValue = computeFloatValue(wholeDigits, fractionDigits, exponentDigits, exponentSign);
            if (type != PascalTokenType.ERROR) {
                value = new Float(floatValue);
            }
        }

    }

    /**
     * 无符号纯数字整数部分
     *
     * @param textBuffer
     * @return
     * @throws Exception
     */
    private String unsignedIntegerDigits(StringBuilder textBuffer) throws Exception {
        char currentChar = currentChar();

        if (!Character.isDigit(currentChar)) {
            type = PascalTokenType.ERROR;
            value = PascalErrorCode.INVALID_NUMBER;
            return null;
        }

        StringBuilder digits = new StringBuilder();
        while (Character.isDigit(currentChar)) {
            textBuffer.append(currentChar);
            digits.append(currentChar);
            currentChar = nextChar();
        }

        return digits.toString();
    }

    private int computeIntegerValue(String digits) {
        if (digits == null) {
            return 0;
        }

        int integerValue = 0;
        int preValue = -1;
        int index = 0;

        while ((index < digits.length()) && (integerValue >= preValue)) {
            preValue = integerValue;
            integerValue = 10 * integerValue + Character.getNumericValue(digits.charAt(index));
            index = index + 1;
        }

        if (integerValue >= preValue) {
            return integerValue;
        } else {
            type = PascalTokenType.ERROR;
            value = PascalErrorCode.RANGE_INTEGER;
            return 0;
        }
    }

    private float computeFloatValue(
            String wholeDigits,
            String fractionDigits,
            String exponentDigits,
            char exponentSign
    ) {
        double floatValue = 0.0;

        //合成整数
        int exponentValue = computeIntegerValue(exponentDigits);
        String digits = wholeDigits;

        if (exponentSign == '-') {
            exponentValue = -exponentValue;
        }

        if (fractionDigits != null) {
            exponentValue -= fractionDigits.length();
            digits += fractionDigits;
        }

        //检查范围
        if (Math.abs(exponentValue + wholeDigits.length()) > MAX_EXPONENT) {
            type = PascalTokenType.ERROR;
            value = PascalErrorCode.RANGE_REAL;
            return 0.0f;
        }

        int index = 0;
        while (index < digits.length()) {
            floatValue = 10 * floatValue + Character.getNumericValue(digits.charAt(index));
            index = index + 1;
        }

        if (exponentValue != 0) {
            floatValue *= Math.pow(10, exponentValue);
        }

        return (float) floatValue;
    }
}
