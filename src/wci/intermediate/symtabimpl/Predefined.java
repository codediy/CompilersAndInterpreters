package wci.intermediate.symtabimpl;

import wci.intermediate.SymTabEntry;
import wci.intermediate.SymTabStack;
import wci.intermediate.TypeFactory;
import wci.intermediate.TypeSpec;
import wci.intermediate.typeimpl.TypeKeyImpl;

import java.util.ArrayList;

import static wci.intermediate.typeimpl.TypeFormImpl.*;

public class Predefined {
    // Predefined types.
    public static TypeSpec integerType;
    public static TypeSpec realType;
    public static TypeSpec booleanType;
    public static TypeSpec charType;
    public static TypeSpec undefinedType;

    // Predefined identifiers.
    public static SymTabEntry integerId;
    public static SymTabEntry realId;
    public static SymTabEntry booleanId;
    public static SymTabEntry charId;
    public static SymTabEntry falseId;
    public static SymTabEntry trueId;


    public static void initialize(SymTabStack symTabStack) {
        initializeTypes(symTabStack);
        initializeConstants(symTabStack);
    }

    /**
     * 预定义基础类型 integer,real,boolean,char,undefined
     *
     * @param symTabStack
     */
    private static void initializeTypes(SymTabStack symTabStack) {
        /**
         * symTabEntry(integer)
         *           ->def = TYPE
         *           ->typeSpec =
         * TypeSpec(SCALAR)
         *          ->id = integerId
         */
        integerId = symTabStack.enterLocal("integer");
        integerType = TypeFactory.createType(SCALAR);
        integerType.setIdentifier(integerId);
        integerId.setDefinition(DefinitionImpl.TYPE);
        integerId.setTypeSpec(integerType);

        realId = symTabStack.enterLocal("real");
        realType = TypeFactory.createType(SCALAR);
        realType.setIdentifier(realId);
        realId.setDefinition(DefinitionImpl.TYPE);
        realId.setTypeSpec(realType);

        booleanId = symTabStack.enterLocal("boolean");
        booleanType = TypeFactory.createType(ENUMERATION);
        booleanType.setIdentifier(booleanId);
        booleanId.setDefinition(DefinitionImpl.TYPE);
        booleanId.setTypeSpec(booleanType);

        charId = symTabStack.enterLocal("char");
        charType = TypeFactory.createType(SCALAR);
        charType.setIdentifier(charId);
        charId.setDefinition(DefinitionImpl.TYPE);
        charId.setTypeSpec(charType);

        undefinedType = TypeFactory.createType(SCALAR);
    }

    /**
     * 预定义 常量 false,true
     *
     * @param symTabStack
     */
    private static void initializeConstants(SymTabStack symTabStack) {
        falseId = symTabStack.enterLocal("false");
        falseId.setDefinition(DefinitionImpl.ENUMERATION_CONSTANT);
        falseId.setTypeSpec(booleanType);
        falseId.setAttribute(SymTabKeyImpl.CONSTANT_VALUE, new Integer(0));

        trueId = symTabStack.enterLocal("true");
        trueId.setDefinition(DefinitionImpl.ENUMERATION_CONSTANT);
        trueId.setTypeSpec(booleanType);
        trueId.setAttribute(SymTabKeyImpl.CONSTANT_VALUE, new Integer(1));

        /**
         * 记录枚举类型booleanType的值
         */
        ArrayList<SymTabEntry> constants = new ArrayList<SymTabEntry>();
        constants.add(falseId);
        constants.add(trueId);
        booleanType.setAttribute(TypeKeyImpl.ENUMERATION_CONSTANTS, constants);

    }
}
