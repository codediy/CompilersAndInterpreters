package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.SymTabEntry;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;

public class BlockParser extends PascalParserTD {
    public BlockParser(PascalParserTD parent) {
        super(parent);
    }

    public ICodeNode parse(Token token, SymTabEntry routineId) throws Exception {

        DeclarationsParser declarationsParser = new DeclarationsParser(this);
        StatementParser statementParser = new StatementParser(this);

        declarationsParser.parse(token);

        token = synchronize(StatementParser.STMT_START_SET);

        TokenType tokenType = token.getType();
        ICodeNode rootNode = null;

        //Begin 语句
        if (tokenType == PascalTokenType.BEGIN) {
            rootNode = statementParser.parse(token);
        } else {
            errorHandler.flag(
                    token,
                    PascalErrorCode.MISSING_BEGIN,
                    this
            );
            if (StatementParser.STMT_START_SET.contains(tokenType)) {
                rootNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.COMPOUND);
                statementParser.parseList(token, rootNode,
                        PascalTokenType.END, PascalErrorCode.MISSING_END
                );
            }
        }

        return rootNode;
    }
}
