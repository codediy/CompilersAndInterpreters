package wci.intermediate.typeimpl;

import wci.intermediate.SymTabEntry;
import wci.intermediate.TypeForm;
import wci.intermediate.TypeKey;
import wci.intermediate.TypeSpec;
import wci.intermediate.symtabimpl.Predefined;

import java.util.HashMap;

import static wci.intermediate.typeimpl.TypeFormImpl.ARRAY;
import static wci.intermediate.typeimpl.TypeFormImpl.SUBRANGE;
import static wci.intermediate.typeimpl.TypeKeyImpl.*;

public class TypeSpecImpl
        extends HashMap<TypeKey, Object>
        implements TypeSpec {

    private TypeForm form;
    private SymTabEntry identifier;

    public TypeSpecImpl(TypeForm form) {
        this.form = form;
        this.identifier = null;
    }

    /**
     * 字符串类型
     *
     * @param value
     */
    public TypeSpecImpl(String value) {
        this.form = ARRAY;

        TypeSpec indexType = new TypeSpecImpl(SUBRANGE);
        indexType.setAttribute(SUBRANGE_BASE_TYPE, Predefined.integerType);
        indexType.setAttribute(SUBRANGE_MIN_VALUE, 1);
        indexType.setAttribute(SUBRANGE_MAX_VALUE, value.length());

        setAttribute(ARRAY_INDEX_TYPE, indexType);
        setAttribute(ARRAY_ELEMENT_TYPE, Predefined.charType);
        setAttribute(ARRAY_ELEMENT_COUNT, value.length());
    }

    @Override
    public TypeForm getForm() {
        return form;
    }

    @Override
    public void setIdentifier(SymTabEntry identifier) {
        this.identifier = identifier;
    }

    @Override
    public SymTabEntry getIdentifier() {
        return identifier;
    }

    @Override
    public void setAttribute(TypeKey key, Object value) {
        this.put(key, value);
    }

    @Override
    public Object getAttribute(TypeKey key) {
        return this.get(key);
    }

    @Override
    public boolean isPascalString() {
        if (form == ARRAY) {
            TypeSpec elmtType = (TypeSpec) getAttribute(ARRAY_ELEMENT_TYPE);
            TypeSpec indexType = (TypeSpec) getAttribute(ARRAY_INDEX_TYPE);

            return (elmtType.baseType() == Predefined.charType)
                    && (indexType.baseType() == Predefined.integerType);
        } else {
            return false;
        }
    }

    @Override
    public TypeSpec baseType() {
        return form == SUBRANGE
                ? (TypeSpec) getAttribute(SUBRANGE_BASE_TYPE)
                : this;
    }

    @Override
    public String toString() {
        return identifier == null ? "null" : identifier.toString();
    }
}
