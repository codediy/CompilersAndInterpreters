package wci.intermediate.symtabimpl;

import wci.intermediate.SymTab;
import wci.intermediate.SymTabEntry;
import wci.intermediate.SymTabKey;

import java.util.ArrayList;
import java.util.HashMap;

public class SymTabEntryImpl extends HashMap<SymTabKey, Object> implements SymTabEntry {

    private String name;
    private SymTab symTab;
    private ArrayList<Integer> lineNumbers;

    public SymTabEntryImpl(String name, SymTab symTab) {
        this.name = name;
        this.symTab = symTab;
        this.lineNumbers = new ArrayList<Integer>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SymTab getSymTab() {
        return symTab;
    }

    @Override
    public void appendLineNumber(int lineNumber) {
        lineNumbers.add(lineNumber);
    }

    @Override
    public ArrayList<Integer> getLineNumber() {
        return lineNumbers;
    }

    @Override
    public void setAttribute(SymTabKey key, Object value) {
        put(key, value);
    }

    @Override
    public Object getAttribute(SymTabKey key) {
        return get(key);
    }
}
