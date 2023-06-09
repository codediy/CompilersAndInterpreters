package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.icodeimpl.ICodeKeyImpl;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;

public class CompoundStatementParser extends StatementParser {
    public CompoundStatementParser(PascalParserTD parent) {
        super(parent);
    }

    public ICodeNode parse(Token token) throws Exception {

        ICodeNode compoundNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.COMPOUND);
        compoundNode.setAttribute(ICodeKeyImpl.LINE, token.getLineNum());


        token = nextToken();
        StatementParser statementParser = new StatementParser(this);
        statementParser.parseList(
                token,
                compoundNode,
                PascalTokenType.END,
                PascalErrorCode.MISSING_END
        );

        return compoundNode;
    }
}
