package wci.frontend.pascal.tokens;

import wci.frontend.Source;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalToken;
import wci.frontend.pascal.PascalTokenType;

public class PascalSpecialSymbolToken extends PascalToken {
    public PascalSpecialSymbolToken(Source source) throws Exception {
        super(source);
    }

    protected void extract() throws Exception {
        char currentChar = currentChar();

        text = Character.toString(currentChar);
        type = null;

        switch (currentChar) {
            case '+':
            case '-':
            case '*':
            case '/':
            case ',':
            case ';':
            case '\'':
            case '=':
            case '(':
            case ')':
            case '[':
            case ']':
            case '{':
            case '}':
            case '^': {
                nextChar();
                break;
            }

            case ':': {
                currentChar = nextChar();
                if (currentChar == '=') {
                    text += currentChar;
                    nextChar();
                }
                break;
            }

            case '<': {
                currentChar = nextChar();
                if (currentChar == '=') {
                    text += currentChar;
                    nextChar();
                } else if (currentChar == '>') {
                    text += currentChar;
                    nextChar();
                }
                break;
            }

            case '>': {
                currentChar = nextChar();
                if (currentChar == '=') {
                    text += currentChar;
                    nextChar();
                }
                break;
            }

            case '.': {
                currentChar = nextChar();
                if (currentChar == '.') {
                    text += currentChar;
                    nextChar();
                }
                break;
            }

            default: {
                nextChar();
                type = PascalTokenType.ERROR;
                value = PascalErrorCode.INVALID_CHARACTER;
            }
        }

        if (type == null) {
            type = PascalTokenType.SPECIAL_SYMBOLS.get(text);
        }
    }
}
