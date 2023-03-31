package wci.intermediate;

import java.util.ArrayList;

public interface SymTabEntry {

    public String getName();

    public SymTab getSymTab();

    public void setDefinition(Definition definition);

    public Definition getDefinition();

    public void setTypeSpec(TypeSpec typeSpec);

    public TypeSpec getTypeSpec();

    public void appendLineNumber(int lineNumber);

    public ArrayList<Integer> getLineNumbers();

    public void setAttribute(SymTabKey key, Object value);

    public Object getAttribute(SymTabKey key);
}
