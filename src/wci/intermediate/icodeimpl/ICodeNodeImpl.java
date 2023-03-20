package wci.intermediate.icodeimpl;

import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeKey;
import wci.intermediate.ICodeNode;
import wci.intermediate.ICodeNodeType;

import java.util.*;

public class ICodeNodeImpl extends HashMap<ICodeKey, Object> implements ICodeNode {

    private ICodeNodeType type;
    private ICodeNode parent;
    private ArrayList<ICodeNode> children;


    public ICodeNodeImpl(ICodeNodeType type) {
        this.type = type;
        this.parent = null;
        this.children = new ArrayList<ICodeNode>();
    }

    @Override
    public ICodeNodeType getType() {
        return type;
    }

    @Override
    public ICodeNode getParent() {
        return parent;
    }

    @Override
    public ICodeNode addChild(ICodeNode node) {
        if (node != null) {
            children.add(node);
            //记录父级节点
            ((ICodeNodeImpl) node).parent = this;
        }
        return node;
    }

    @Override
    public ArrayList<ICodeNode> getChildren() {
        return children;
    }

    @Override
    public void setAttribute(ICodeKey key, Object value) {
        put(key, value);
    }

    @Override
    public Object getAttribute(ICodeKey key) {
        return get(key);
    }

    @Override
    public ICodeNode copy() {
        ICodeNodeImpl copy = (ICodeNodeImpl) ICodeFactory.createICodeNode(type);

        Set<Entry<ICodeKey, Object>> attributes = entrySet();
        Iterator<Entry<ICodeKey, Object>> it = attributes.iterator();

        while (it.hasNext()) {
            Map.Entry<ICodeKey, Object> attribute = it.next();
            copy.put(attribute.getKey(), attribute.getValue());
        }
        return copy;
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
