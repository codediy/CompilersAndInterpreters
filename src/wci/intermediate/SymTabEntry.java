package wci.intermediate;

import java.util.ArrayList;

public interface SymTabEntry {

    public String getName();

    public SymTab getSymTab();

    public void appendLineNumber(int lineNumber);

    public ArrayList<Integer> getLineNumber();

    public void setAttribute(SymTabKey key, Object value);

    public Object getAttribute(SymTabKey key);
}
