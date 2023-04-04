package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.SymTabEntry;
import wci.intermediate.TypeSpec;
import wci.intermediate.icodeimpl.ICodeKeyImpl;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;
import wci.intermediate.symtabimpl.Predefined;
import wci.intermediate.typeimpl.TypeChecker;

import java.util.EnumSet;

import static wci.frontend.pascal.PascalTokenType.*;

public class AssignmentStatementParser extends StatementParser {
    public AssignmentStatementParser(PascalParserTD parent) {
        super(parent);
    }

    private static final EnumSet<PascalTokenType> COLON_EQUALS_SET =
            ExpressionParser.EXPR_START_SET.clone();

    static {
        COLON_EQUALS_SET.add(COLON_EQUALS);
        COLON_EQUALS_SET.addAll(StatementParser.STMT_FOLLOW_SET);
    }

    public ICodeNode parse(Token token) throws Exception {
        // assignNode (id := express)
        ICodeNode assignNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.ASSIGN);
        assignNode.setAttribute(ICodeKeyImpl.LINE, token.getLineNum());

        // Id
//        String targetName = token.getText().toLowerCase();
//        SymTabEntry targetId = symTabStack.lookup(targetName);
//
//        if (targetId == null) {
//            targetId = symTabStack.enterLocal(targetName);
//        }
//        targetId.appendLineNumber(token.getLineNum());
//
//        ICodeNode variableNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.VARIABLE);
//        variableNode.setAttribute(ICodeKeyImpl.ID, targetId);
//        assignNode.addChild(variableNode);

        VariableParser variableParser = new VariableParser(this);
        ICodeNode targetNode = variableParser.parse(token);
        TypeSpec targetType = targetNode != null
                ? targetNode.getTypeSpec()
                : Predefined.undefinedType;
        assignNode.addChild(targetNode);

        // :=
//        token = nextToken();

        // 预测等于号
        token = synchronize(COLON_EQUALS_SET);

        if (token.getType() == COLON_EQUALS) {
            token = nextToken();
        } else {
            errorHandler.flag(token, PascalErrorCode.MISSING_COLON_EQUALS, this);
        }


        // expression
        ExpressionParser expressionParser = new ExpressionParser(this);
        ICodeNode exprNode = expressionParser.parse(token);
        assignNode.addChild(exprNode);
        TypeSpec exprType = exprNode != null
                ? exprNode.getTypeSpec()
                : Predefined.undefinedType;

        if (!TypeChecker.areAssignmentCompatible(
                targetType,
                exprType
        )) {
            errorHandler.flag(
                    token,
                    PascalErrorCode.INCOMPATIBLE_TYPES,
                    this
            );
        }
        assignNode.setTypeSpec(targetType);
        return assignNode;
    }
}
