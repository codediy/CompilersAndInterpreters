package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.TypeSpec;
import wci.intermediate.icodeimpl.ICodeKeyImpl;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;
import wci.intermediate.symtabimpl.Predefined;
import wci.intermediate.typeimpl.TypeChecker;

public class RepeatStatementParser extends StatementParser {
    public RepeatStatementParser(PascalParserTD parent) {
        super(parent);
    }

    @Override
    public ICodeNode parse(Token token) throws Exception {


        ICodeNode loopNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.LOOP);
        ICodeNode testNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.TEST);
        //REPEAT的起始行
        loopNode.setAttribute(ICodeKeyImpl.LINE, token.getLineNum());

        //statementList
        token = nextToken(); //跳过REPEAT
        StatementParser statementParser = new StatementParser(this);
        statementParser.parseList(
                token,
                loopNode,
                PascalTokenType.UNTIL,
                PascalErrorCode.MISSING_UNTIL
        );

        token = currentToken();

        //expression
        ExpressionParser expressionParser = new ExpressionParser(this);
        ICodeNode exprNode = expressionParser.parse(token);

        testNode.addChild(exprNode);
        loopNode.addChild(testNode);

        TypeSpec exprType = exprNode != null
                ? exprNode.getTypeSpec()
                : Predefined.undefinedType;
        if (!TypeChecker.isBoolean(exprType)) {
            errorHandler.flag(
                    token,
                    PascalErrorCode.INCOMPATIBLE_TYPES,
                    this
            );
        }
        return loopNode;
    }
}
