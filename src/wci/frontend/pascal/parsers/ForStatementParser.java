package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.TypeForm;
import wci.intermediate.TypeSpec;
import wci.intermediate.icodeimpl.ICodeKeyImpl;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;
import wci.intermediate.symtabimpl.Predefined;
import wci.intermediate.typeimpl.TypeChecker;
import wci.intermediate.typeimpl.TypeFormImpl;

import java.util.EnumSet;

import static wci.frontend.pascal.PascalTokenType.*;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.*;

public class ForStatementParser extends StatementParser {
    public ForStatementParser(PascalParserTD parent) {
        super(parent);
    }

    private static final EnumSet<PascalTokenType> TO_DOWNTO_SET =
            ExpressionParser.EXPR_START_SET.clone();

    static {
        TO_DOWNTO_SET.add(TO);
        TO_DOWNTO_SET.add(DOWNTO);
        TO_DOWNTO_SET.addAll(StatementParser.STMT_FOLLOW_SET);
    }

    private static final EnumSet<PascalTokenType> DO_SET =
            StatementParser.STMT_START_SET.clone();

    static {
        DO_SET.add(DO);
        DO_SET.addAll(StatementParser.STMT_FOLLOW_SET);
    }

    /**
     * CompoundNode
     * InitAssignNode
     * LoopNode
     * TestNode
     * StatementList
     * NextAssignNode
     *
     * @param token
     * @return
     * @throws Exception
     */
    @Override
    public ICodeNode parse(Token token) throws Exception {


        ICodeNode compoundNode = ICodeFactory.createICodeNode(
                ICodeNodeTypeImpl.COMPOUND
        );
        compoundNode.setAttribute(ICodeKeyImpl.LINE, token.getLineNum());

        ICodeNode loopNode = ICodeFactory.createICodeNode(
                ICodeNodeTypeImpl.LOOP
        );
        ICodeNode testNode = ICodeFactory.createICodeNode(
                ICodeNodeTypeImpl.TEST
        );

        // For
        token = nextToken();
        Token targetToken = token;

        // k := j
        AssignmentStatementParser assignmentStatementParser =
                new AssignmentStatementParser(this);
        ICodeNode initAssignNode = assignmentStatementParser.parse(token);
        TypeSpec controlType = initAssignNode != null
                ? initAssignNode.getTypeSpec()
                : Predefined.undefinedType;

        setLineNumber(initAssignNode, targetToken);
        if (!TypeChecker.isInteger(controlType)
                && (controlType.getForm() != TypeFormImpl.ENUMERATION)) {
            errorHandler.flag(
                    token,
                    PascalErrorCode.INCOMPATIBLE_TYPES,
                    this
            );
        }
        // init
        compoundNode.addChild(initAssignNode);
        // loop
        compoundNode.addChild(loopNode);

        //TO/ DOWNTO
        token = synchronize(TO_DOWNTO_SET);
        TokenType direction = token.getType();

        if ((direction == TO) || (direction == DOWNTO)) {
            token = nextToken();
        } else {
            direction = TO;
            errorHandler.flag(
                    token,
                    PascalErrorCode.MISSING_TO_DOWNTO,
                    this
            );
        }

        // 比较运算 TEST
        ICodeNode relOpNode = ICodeFactory.createICodeNode(
                direction == TO ? GT : LT
        );
        relOpNode.setTypeSpec(Predefined.booleanType);

        ICodeNode controlVarNode = initAssignNode.getChildren().get(0);
        relOpNode.addChild(controlVarNode.copy());

        ExpressionParser expressionParser = new ExpressionParser(this);
        ICodeNode exprNode = expressionParser.parse(token);
        relOpNode.addChild(exprNode);

        TypeSpec exprType = exprNode != null
                ? exprNode.getTypeSpec()
                : Predefined.undefinedType;
        if (!TypeChecker.areAssignmentCompatible(controlType, exprType)) {
            errorHandler.flag(
                    token,
                    PascalErrorCode.INCOMPATIBLE_TYPES,
                    this
            );
        }


        testNode.addChild(relOpNode);
        loopNode.addChild(testNode);

        token = synchronize(DO_SET);
        if (token.getType() == DO) {
            token = nextToken();
        } else {
            errorHandler.flag(
                    token,
                    PascalErrorCode.MISSING_DO,
                    this
            );
            ;
        }

        // statementList
        StatementParser statementParser = new StatementParser(this);
        loopNode.addChild(statementParser.parse(token));


        // 递增
        // k :=
        ICodeNode nextAssignNode = ICodeFactory.createICodeNode(ASSIGN);
        nextAssignNode.addChild(controlVarNode.copy());

        // k + 1
        ICodeNode arithOpNode = ICodeFactory.createICodeNode(
                direction == TO ? ADD : SUBTRACT
        );
        arithOpNode.addChild(controlVarNode.copy());

        ICodeNode oneNode = ICodeFactory.createICodeNode(INTEGER_CONSTANT);
        oneNode.setAttribute(ICodeKeyImpl.VALUE, 1);
        arithOpNode.addChild(oneNode);

        nextAssignNode.addChild(arithOpNode);
        loopNode.addChild(nextAssignNode);

        setLineNumber(nextAssignNode, targetToken);

        return compoundNode;

    }
}
