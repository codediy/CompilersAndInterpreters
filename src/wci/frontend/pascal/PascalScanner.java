package wci.frontend.pascal;

import wci.frontend.EofToken;
import wci.frontend.Scanner;
import wci.frontend.Source;
import wci.frontend.Token;
import wci.frontend.pascal.tokens.*;

public class PascalScanner extends Scanner {
    public PascalScanner(Source source) {
        super(source);
    }

    @Override
    protected Token extractToken() throws Exception {
        skipWhiteSpace();

        Token token;
        char currentChar = currentChar();
        if (currentChar == Source.EOF) {
            //EOF
            token = new EofToken(source);
        } else if (Character.isLetter(currentChar)) {
            //标记符 保留字
            token = new PascalWordToken(source);
        } else if (Character.isDigit(currentChar)) {
            //数字
            token = new PascalNumberToken(source);
        } else if (currentChar == '\'') {
            //字符串
            token = new PascalStringToken(source);
        } else if (PascalTokenType.SPECIAL_SYMBOLS.containsKey(Character.toString(currentChar))) {
            //运算符
            token = new PascalSpecialSymbolToken(source);
        } else {
            //其他 错误
            token = new PascalErrorToken(source, PascalErrorCode.INVALID_CHARACTER, Character.toString(currentChar));
            nextChar();
        }

        return token;
    }

    private void skipWhiteSpace() throws Exception {
        char currentChar = currentChar();
        while (Character.isWhitespace(currentChar) || (currentChar == '{')) {
            //注释
            if (currentChar == '{') {
                do {
                    currentChar = nextChar();
                } while ((currentChar != '}') && (currentChar != Source.EOF));

                if (currentChar == '}') {
                    currentChar = nextChar();
                }
            } else {
                currentChar = nextChar();
            }
        }
    }
}
