package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.*;
import wci.intermediate.icodeimpl.ICodeKeyImpl;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;
import wci.intermediate.symtabimpl.DefinitionImpl;
import wci.intermediate.symtabimpl.Predefined;
import wci.intermediate.typeimpl.TypeChecker;
import wci.intermediate.typeimpl.TypeFormImpl;
import wci.intermediate.typeimpl.TypeKeyImpl;

import java.util.EnumSet;

import static wci.frontend.pascal.PascalTokenType.*;

public class VariableParser extends StatementParser {
    public VariableParser(PascalParserTD parent) {
        super(parent);
    }

    private static final EnumSet<PascalTokenType> SUBSCRIPT_FIELD_START_SET =
            EnumSet.of(LEFT_BRACKET, DOT);

    public ICodeNode parse(Token token) throws Exception {
        String name = token.getText().toLowerCase();

        SymTabEntry variableId = symTabStack.lookup(name);

        if (variableId == null) {
            errorHandler.flag(
                    token,
                    PascalErrorCode.IDENTIFIER_UNDEFINED,
                    this
            );
            variableId = symTabStack.enterLocal(name);
            variableId.setDefinition(DefinitionImpl.UNDEFINED);
            variableId.setTypeSpec(Predefined.undefinedType);
        }
        return parse(token, variableId);
    }

    public ICodeNode parse(
            Token token,
            SymTabEntry variableId
    ) throws Exception {
        Definition defnCode = variableId.getDefinition();
        if ((defnCode != DefinitionImpl.VARIABLE)
                && (defnCode != DefinitionImpl.VALUE_PARM)
                && (defnCode != DefinitionImpl.VAR_PARM)) {
            errorHandler.flag(
                    token,
                    PascalErrorCode.INVALID_IDENTIFIER_USAGE,
                    this
            );
        }
        variableId.appendLineNumber(token.getLineNumber());

        ICodeNode variableNode = ICodeFactory.createICodeNode(
                ICodeNodeTypeImpl.VARIABLE
        );
        variableNode.setAttribute(ICodeKeyImpl.ID, variableId);

        token = nextToken();

        TypeSpec variableType = variableId.getTypeSpec();
        while (SUBSCRIPT_FIELD_START_SET.contains(token.getType())) {
            ICodeNode subFldNode = token.getType() == LEFT_BRACKET
                    ? parseSubscripts(variableType)
                    : parseField(variableType);
            token = currentToken();

            variableType = subFldNode.getTypeSpec();
            variableNode.addChild(subFldNode);
        }

        variableNode.setTypeSpec(variableType);
        return variableNode;
    }

    private static final EnumSet<PascalTokenType> RIGHT_BRACKET_SET =
            EnumSet.of(RIGHT_BRACKET, EQUALS, SEMICOLON);

    /**
     * 数组内容
     *
     * @param variableType
     * @return
     * @throws Exception
     */
    private ICodeNode parseSubscripts(TypeSpec variableType)
            throws Exception {
        Token token;
        ExpressionParser expressionParser = new ExpressionParser(this);

        ICodeNode subscriptNode = ICodeFactory.createICodeNode(
                ICodeNodeTypeImpl.SUBSCRIPTS
        );

        do {
            token = nextToken();

            if (variableType.getForm() == TypeFormImpl.ARRAY) {
                ICodeNode exprNode = expressionParser.parse(token);

                TypeSpec exprType = exprNode != null
                        ? exprNode.getTypeSpec()
                        : Predefined.undefinedType;

                TypeSpec indexType = (TypeSpec) variableType.getAttribute(
                        TypeKeyImpl.ARRAY_INDEX_TYPE
                );
                if (!TypeChecker.areAssignmentCompatible(indexType, exprType)) {
                    errorHandler.flag(
                            token,
                            PascalErrorCode.INCOMPATIBLE_TYPES,
                            this
                    );
                }
                subscriptNode.addChild(exprNode);
                variableType = (TypeSpec) variableType.getAttribute(
                        TypeKeyImpl.ARRAY_ELEMENT_TYPE
                );
            } else {
                errorHandler.flag(token,
                        PascalErrorCode.TOO_MANY_SUBSCRIPTS,
                        this);
                expressionParser.parse(token);
            }
            token = currentToken();
            ;
        } while (token.getType() == COMMA);

        token = synchronize(RIGHT_BRACKET_SET);
        if (token.getType() == RIGHT_BRACKET) {
            token = nextToken();
        } else {
            errorHandler.flag(
                    token,
                    PascalErrorCode.MISSING_RIGHT_BRACKET,
                    this
            );
        }
        subscriptNode.setTypeSpec(variableType);
        return subscriptNode;
    }

    /**
     * Record的field字段
     *
     * @param variableType
     * @return
     * @throws Exception
     */
    private ICodeNode parseField(TypeSpec variableType)
            throws Exception {
        ICodeNode fieldNode = ICodeFactory.createICodeNode(
                ICodeNodeTypeImpl.FIELD
        );

        Token token = nextToken();
        TokenType tokenType = token.getType();
        TypeForm variableForm = variableType.getForm();

        if ((tokenType == IDENTIFIER) && (variableForm == TypeFormImpl.RECORD)) {
            SymTab symTab = (SymTab) variableType.getAttribute(TypeKeyImpl.RECORD_SYMTAB);
            String fieldName = token.getText().toLowerCase();
            SymTabEntry fieldId = symTab.lookup(fieldName);

            if (fieldId != null) {
                variableType = fieldId.getTypeSpec();
                fieldId.appendLineNumber(token.getLineNumber());

                fieldNode.setAttribute(ICodeKeyImpl.ID, fieldId);
            } else {
                errorHandler.flag(
                        token,
                        PascalErrorCode.INVALID_FIELD,
                        this
                );
            }
        } else {
            errorHandler.flag(
                    token,
                    PascalErrorCode.INVALID_FIELD,
                    this
            );
        }

        token = nextToken();
        fieldNode.setTypeSpec(variableType);
        return fieldNode;
    }
}
