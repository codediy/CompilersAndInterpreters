package wci.util;

import wci.intermediate.ICode;
import wci.intermediate.ICodeKey;
import wci.intermediate.ICodeNode;
import wci.intermediate.SymTabEntry;
import wci.intermediate.icodeimpl.ICodeNodeImpl;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ParseTreePrinter {
    private static final int INDENT_WIDTH = 4;
    private static final int LINE_WIDTH = 80;

    private PrintStream ps;      // output print stream
    private int length;          // output line length
    private String indent;       // indent spaces
    private String indentation;  // indentation of a line
    private StringBuilder line;  // output line

    public ParseTreePrinter(PrintStream ps) {
        this.ps = ps;
        this.length = 0;
        this.indentation = "";
        this.line = new StringBuilder();

        // The indent is INDENT_WIDTH spaces.
        this.indent = "";
        for (int i = 0; i < INDENT_WIDTH; ++i) {
            this.indent += " ";
        }
    }

    public void print(ICode iCode) {
        ps.println("\n===== INTERMEDIATE CODE =====\n");

        printNode((ICodeNodeImpl) iCode.getRoot());
        printLine();
    }

    private void printNode(ICodeNodeImpl node) {
        // Opening tag.
        append(indentation);
        append("<" + node.toString());

        printAttributes(node);
        printTypeSpec(node);

        ArrayList<ICodeNode> childNodes = node.getChildren();

        // Print the node's children followed by the closing tag.
        if ((childNodes != null) && (childNodes.size() > 0)) {
            append(">");
            printLine();

            printChildNodes(childNodes);
            append(indentation);
            append("</" + node.toString() + ">");
        }

        // No children: Close off the tag.
        else {
            append(" ");
            append("/>");
        }

        printLine();
    }

    private void printAttributes(ICodeNodeImpl node) {
        String saveIndentation = indentation;
        indentation += indent;

        Set<Map.Entry<ICodeKey, Object>> attributes = node.entrySet();
        Iterator<Map.Entry<ICodeKey, Object>> it = attributes.iterator();

        // Iterate to print each attribute.
        while (it.hasNext()) {
            Map.Entry<ICodeKey, Object> attribute = it.next();
            printAttribute(attribute.getKey().toString(), attribute.getValue());
        }

        indentation = saveIndentation;
    }

    private void printAttribute(String keyString, Object value) {
        // If the value is a symbol table entry, use the identifier's name.
        // Else just use the value string.
        boolean isSymTabEntry = value instanceof SymTabEntry;
        String valueString = isSymTabEntry ? ((SymTabEntry) value).getName()
                : value.toString();

        String text = keyString.toLowerCase() + "=\"" + valueString + "\"";
        append(" ");
        append(text);

        // Include an identifier's nesting level.
        if (isSymTabEntry) {
            int level = ((SymTabEntry) value).getSymTab().getNestingLevel();
            printAttribute("LEVEL", level);
        }
    }

    private void printChildNodes(ArrayList<ICodeNode> childNodes) {
        String saveIndentation = indentation;
        indentation += indent;

        for (ICodeNode child : childNodes) {
            printNode((ICodeNodeImpl) child);
        }

        indentation = saveIndentation;
    }

    private void printTypeSpec(ICodeNodeImpl node) {
    }

    private void append(String text) {
        int textLength = text.length();
        boolean lineBreak = false;

        // Wrap lines that are too long.
        if (length + textLength > LINE_WIDTH) {
            printLine();
            line.append(indentation);
            length = indentation.length();
            lineBreak = true;
        }

        // Append the text.
        if (!(lineBreak && text.equals(" "))) {
            line.append(text);
            length += textLength;
        }
    }

    private void printLine() {
        if (length > 0) {
            ps.println(line);
            line.setLength(0);
            length = 0;
        }
    }
}
