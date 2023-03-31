package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.symtabimpl.DefinitionImpl;

import java.util.EnumSet;

import static wci.frontend.pascal.PascalTokenType.*;

public class DeclarationsParser extends PascalParserTD {
    public DeclarationsParser(PascalParserTD parent) {
        super(parent);
    }

    /**
     * declaration start
     * const type var procedure function begin
     */
    static final EnumSet<PascalTokenType> DECLARATION_START_SET =
            EnumSet.of(CONST, TYPE, VAR, PROCEDURE, FUNCTION, BEGIN);

    /**
     * type start
     * type var procedure function begin
     */
    static final EnumSet<PascalTokenType> TYPE_START_SET =
            DECLARATION_START_SET.clone();

    static {
        TYPE_START_SET.remove(CONST);
    }

    /**
     * var start
     * var procedure function begin
     */
    static final EnumSet<PascalTokenType> VAR_START_SET =
            TYPE_START_SET.clone();

    static {
        VAR_START_SET.remove(TYPE);
    }

    /**
     * procedure function begin
     */
    static final EnumSet<PascalTokenType> ROUTINE_START_SET =
            VAR_START_SET.clone();

    static {
        ROUTINE_START_SET.remove(VAR);
    }


    public void parse(Token token) throws Exception {

        token = synchronize(DECLARATION_START_SET);
        if (token.getType() == CONST) {
            //CONST
            token = nextToken();

            ConstantDefinitionsParser constantDefinitionsParser =
                    new ConstantDefinitionsParser(this);
            constantDefinitionsParser.parse(token);
        }

        token = synchronize(TYPE_START_SET);
        if (token.getType() == TYPE) {
            // TYPE
            token = nextToken();

            TypeDefinitionsParser typeDefinitionsParser =
                    new TypeDefinitionsParser(this);
            typeDefinitionsParser.parse(token);
        }

        token = synchronize(VAR_START_SET);
        if (token.getType() == VAR) {
            //VAR
            token = nextToken();
            VariableDeclarationsParser variableDeclarationsParser =
                    new VariableDeclarationsParser(this);
            variableDeclarationsParser.setDefinition(DefinitionImpl.VARIABLE);
            variableDeclarationsParser.parse(token);
        }

        token = synchronize(ROUTINE_START_SET);

    }

}
