package wci.intermediate.symtabimpl;

import wci.intermediate.SymTab;
import wci.intermediate.SymTabEntry;
import wci.intermediate.SymTabFactory;
import wci.intermediate.SymTabStack;

import java.util.ArrayList;

public class SymTabStackImpl extends ArrayList<SymTab> implements SymTabStack {

    private int currentNestingLevel;
    private SymTabEntry programId;

    public SymTabStackImpl() {
        this.currentNestingLevel = 0;
        add(SymTabFactory.createSymTab(currentNestingLevel));
    }

    @Override
    public void setProgramId(SymTabEntry programId) {
        this.programId = programId;
    }

    @Override
    public SymTabEntry getProgramId() {
        return programId;
    }

    @Override
    public int getCurrentNestingLevel() {
        return currentNestingLevel;
    }

    @Override
    public SymTab getLocalSymTab() {
        return get(currentNestingLevel);
    }

    @Override
    public SymTab push() {
        currentNestingLevel = currentNestingLevel + 1;
        SymTab symTab = SymTabFactory.createSymTab(currentNestingLevel);
        add(symTab);
        return symTab;
    }

    @Override
    public SymTab push(SymTab symTab) {
        currentNestingLevel = currentNestingLevel + 1;
        add(symTab);
        return symTab;
    }

    public SymTab pop() {
        SymTab symTab = get(currentNestingLevel);
        remove(currentNestingLevel);
        currentNestingLevel = currentNestingLevel - 1;
        return symTab;
    }


    @Override
    public SymTabEntry enterLocal(String name) {
        return get(currentNestingLevel).enter(name);
    }

    @Override
    public SymTabEntry lookupLocal(String name) {
        return get(currentNestingLevel).lookup(name);
    }

    @Override
    public SymTabEntry lookup(String name) {
        SymTabEntry foundEntry = null;
        for (int i = currentNestingLevel; (i >= 0) && (foundEntry == null); i = i - 1) {
            foundEntry = get(i).lookup(name);
        }

        return foundEntry;
    }
}
