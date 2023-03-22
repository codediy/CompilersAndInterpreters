package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;

import java.util.EnumSet;

import static wci.frontend.pascal.PascalTokenType.*;

public class IfStatementParser extends StatementParser {
    public IfStatementParser(PascalParserTD parent) {
        super(parent);
    }


    private static final EnumSet<PascalTokenType> THEN_SET =
            StatementParser.STMT_START_SET.clone();

    static {
        THEN_SET.add(THEN);
        THEN_SET.addAll(StatementParser.STMT_FOLLOW_SET);
    }

    @Override
    public ICodeNode parse(Token token) throws Exception {

        ICodeNode ifNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.IF);
        setLineNumber(ifNode, token);

        // if
        token = nextToken();

        // x > y
        ExpressionParser expressionParser = new ExpressionParser(this);
        ifNode.addChild(expressionParser.parse(token));

        // then
        token = synchronize(THEN_SET);
        if (token.getType() == THEN) {
            token = nextToken();
        } else {
            errorHandler.flag(
                    token,
                    PascalErrorCode.MISSING_THEN,
                    this
            );
        }
        // Then statement
        StatementParser statementParser = new StatementParser(this);
        ifNode.addChild(statementParser.parse(token));

        // else?
        token = currentToken();
        if (token.getType() == ELSE) {
            token = nextToken();
            ifNode.addChild(statementParser.parse(token));
        }

        return ifNode;
    }
}
