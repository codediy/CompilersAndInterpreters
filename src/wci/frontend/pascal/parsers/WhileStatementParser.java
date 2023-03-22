package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.icodeimpl.ICodeKeyImpl;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;

import java.util.EnumSet;

import static wci.frontend.pascal.PascalTokenType.*;

public class WhileStatementParser extends StatementParser {
    public WhileStatementParser(PascalParserTD parent) {
        super(parent);
    }


    private static final EnumSet<PascalTokenType> DO_SET =
            StatementParser.STMT_START_SET.clone();

    static {
        DO_SET.add(DO);
        DO_SET.addAll(StatementParser.STMT_FOLLOW_SET);
    }

    @Override
    public ICodeNode parse(Token token) throws Exception {

        ICodeNode loopNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.LOOP);
        loopNode.setAttribute(ICodeKeyImpl.LINE, token.getLineNum());

        ICodeNode breakNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.TEST);
        ICodeNode notNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.NOT);

        loopNode.addChild(breakNode);
        breakNode.addChild(notNode);

        // while
        token = nextToken();

        // expression
        ExpressionParser expressionParser = new ExpressionParser(this);
        notNode.addChild(expressionParser.parse(token));

        // do
        token = synchronize(DO_SET);
        if (token.getType() == DO) {
            token = nextToken();
        } else {
            errorHandler.flag(token,
                    PascalErrorCode.MISSING_DO,
                    this);
        }

        // statement
        StatementParser statementParser = new StatementParser(this);
        loopNode.addChild(statementParser.parse(token));

        return loopNode;

    }
}
