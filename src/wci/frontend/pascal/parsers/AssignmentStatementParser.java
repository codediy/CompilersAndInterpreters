package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.SymTabEntry;
import wci.intermediate.icodeimpl.ICodeKeyImpl;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;

public class AssignmentStatementParser extends StatementParser {
    public AssignmentStatementParser(PascalParserTD parent) {
        super(parent);
    }

    public ICodeNode parse(Token token) throws Exception {
        // assignNode (id := express)
        ICodeNode assignNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.ASSIGN);
        assignNode.setAttribute(ICodeKeyImpl.LINE, token.getLineNum());

        // Id
        String targetName = token.getText().toLowerCase();
        SymTabEntry targetId = symTabStack.lookup(targetName);

        if (targetId == null) {
            targetId = symTabStack.enterLocal(targetName);
        }
        targetId.appendLineNumber(token.getLineNum());

        ICodeNode variableNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.VARIABLE);
        variableNode.setAttribute(ICodeKeyImpl.ID, targetId);

        assignNode.addChild(variableNode);

        // :=
        token = nextToken();
        if (token.getType() == PascalTokenType.COLON_EQUALS) {
            token = nextToken();
        } else {
            errorHandler.flag(token, PascalErrorCode.MISSING_COLON_EQUALS, this);
        }

        // expression
        ExpressionParser expressionParser = new ExpressionParser(this);
        assignNode.addChild(expressionParser.parse(token));

        return assignNode;
    }
}
