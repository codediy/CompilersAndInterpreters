package wci.frontend.pascal.tokens;

import wci.frontend.Source;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalToken;
import wci.frontend.pascal.PascalTokenType;

public class PascalErrorToken extends PascalToken {
    public PascalErrorToken(Source source,
                            PascalErrorCode errorCode,
                            String tokenText

    ) throws Exception {
        super(source);

        this.text = tokenText;
        this.type = PascalTokenType.ERROR;
        this.value = errorCode;
    }

    protected void extract() throws Exception {
    }
}
