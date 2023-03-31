package wci.intermediate;

public interface SymTabStack {

    public void setProgramId(SymTabEntry entry);

    public SymTabEntry getProgramId();


    public int getCurrentNestingLevel();

    public SymTab getLocalSymTab();

    public SymTab push();

    public SymTab push(SymTab symTab);

    public SymTab pop();

    public SymTabEntry enterLocal(String name);

    public SymTabEntry lookupLocal(String name);

    public SymTabEntry lookup(String name);
}
