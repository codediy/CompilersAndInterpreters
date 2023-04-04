package wci.intermediate.typeimpl;

import wci.intermediate.TypeForm;
import wci.intermediate.TypeSpec;
import wci.intermediate.symtabimpl.Predefined;

public class TypeChecker {
    public static boolean isInteger(TypeSpec type) {
        return (type != null) && (type.baseType() == Predefined.integerType);
    }

    public static boolean areBothInteger(TypeSpec type1, TypeSpec type2) {
        return isInteger(type1) && isInteger(type2);
    }

    public static boolean isReal(TypeSpec type) {
        return (type != null) && (type.baseType() == Predefined.realType);
    }

    public static boolean isIntegerOrReal(TypeSpec type) {
        return isInteger(type) || isReal(type);
    }

    public static boolean isAtLeastOneReal(TypeSpec type1, TypeSpec type2) {
        return (isReal(type1) && isReal(type2))
                || (isReal(type1) && isInteger(type2))
                || (isInteger(type1) && isReal(type2));
    }

    public static boolean isBoolean(TypeSpec type) {
        return (type != null) && (type.baseType() == Predefined.booleanType);
    }

    public static boolean areBothBoolean(TypeSpec type1, TypeSpec type2) {
        return isBoolean(type1) && isBoolean(type2);
    }

    public static boolean isChar(TypeSpec type) {
        return (type != null) && (type.baseType() == Predefined.charType);
    }

    public static boolean areAssignmentCompatible(
            TypeSpec targetType,
            TypeSpec valueType
    ) {
        if ((targetType == null) || (valueType == null)) {
            return false;
        }

        targetType = targetType.baseType();
        valueType = valueType.baseType();

        boolean compatible = false;

        if (targetType == valueType) {
            compatible = true;
        } else if (isReal(targetType) && isInteger(valueType)) {
            compatible = true;
        } else {
            compatible = targetType.isPascalString() && valueType.isPascalString();
        }

        return compatible;
    }

    public static boolean areComparisonCompatible(
            TypeSpec type1,
            TypeSpec type2
    ) {
        if ((type1 == null) || (type2 == null)) {
            return false;
        }

        type1 = type1.baseType();
        type2 = type2.baseType();

        TypeForm form = type1.getForm();
        boolean compatible = false;

        if ((type1 == type2) &&
                ((form == TypeFormImpl.SCALAR) || (form == TypeFormImpl.ENUMERATION))
        ) {
            compatible = true;
        } else if (isAtLeastOneReal(type1, type2)) {
            compatible = true;
        } else {
            compatible = type1.isPascalString() && type2.isPascalString();
        }

        return compatible;
    }

}
