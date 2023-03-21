package wci.backend.interpreter.executors;

import wci.backend.interpreter.Executor;
import wci.backend.interpreter.RuntimeErrorCode;
import wci.intermediate.ICodeNode;
import wci.intermediate.SymTabEntry;
import wci.intermediate.icodeimpl.ICodeKeyImpl;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;
import wci.intermediate.symtabimpl.SymTabKeyImpl;

import java.util.ArrayList;
import java.util.EnumSet;

import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.*;

public class ExpressionExecutor extends StatementExecutor {
    public ExpressionExecutor(Executor parent) {
        super(parent);
    }

    public Object execute(ICodeNode node) {
        ICodeNodeTypeImpl nodeType = (ICodeNodeTypeImpl) node.getType();

        switch (nodeType) {
            // 变量的值
            case VARIABLE: {
                SymTabEntry entry = (SymTabEntry) node.getAttribute(ICodeKeyImpl.ID);
                return entry.getAttribute(SymTabKeyImpl.DATA_VALUE);
            }
            case INTEGER_CONSTANT: {
                return (Integer) node.getAttribute(ICodeKeyImpl.VALUE);
            }
            case REAL_CONSTANT: {
                return (Float) node.getAttribute(ICodeKeyImpl.VALUE);
            }
            case STRING_CONSTANT: {
                return (String) node.getAttribute(ICodeKeyImpl.VALUE);
            }
            case NEGATE: {
                ArrayList<ICodeNode> children = node.getChildren();
                ICodeNode expressionNode = children.get(0);
                //求解表达式的值
                Object value = execute(expressionNode);
                if (value instanceof Integer) {
                    return -((Integer) value);
                } else {
                    return -((Float) value);
                }
            }
            case NOT: {
                ArrayList<ICodeNode> children = node.getChildren();
                ICodeNode expressionNode = children.get(0);

                boolean value = (Boolean) execute(expressionNode);
                return !value;
            }
            default: {
                return executeBinaryOperator(node, nodeType);
            }
        }

    }

    private static final EnumSet<ICodeNodeTypeImpl> ARITH_OPS =
            EnumSet.of(ADD, SUBTRACT, MULTIPLY, FLOAT_DIVIDE, INTEGER_DIVIDE, MOD);

    private Object executeBinaryOperator(
            ICodeNode node,
            ICodeNodeTypeImpl nodeType
    ) {
        ArrayList<ICodeNode> children = node.getChildren();

        ICodeNode operandNode1 = children.get(0);
        ICodeNode operandNode2 = children.get(1);

        Object operand1 = execute(operandNode1);
        Object operand2 = execute(operandNode2);

        //数字模式
        boolean integerMode = (operand1 instanceof Integer)
                && (operand2 instanceof Integer);

        //算术运算
        if (ARITH_OPS.contains(nodeType)) {
            if (integerMode) {
                int value1 = (Integer) operand1;
                int value2 = (Integer) operand2;

                switch (nodeType) {
                    case ADD: {
                        return value1 + value2;
                    }
                    case SUBTRACT: {
                        return value1 - value2;
                    }
                    case MULTIPLY: {
                        return value1 * value2;
                    }
                    case FLOAT_DIVIDE: {
                        if (value2 != 0) {
                            return ((float) value1) / ((float) value2);
                        } else {
                            errorHandler.flag(
                                    node,
                                    RuntimeErrorCode.DIVISION_BY_ZERO,
                                    this
                            );
                            return 0;
                        }
                    }
                    case INTEGER_DIVIDE: {
                        if (value2 != 0) {
                            return value1 / value2;
                        } else {
                            errorHandler.flag(
                                    node,
                                    RuntimeErrorCode.DIVISION_BY_ZERO,
                                    this
                            );
                            return 0;
                        }
                    }
                    case MOD: {
                        if (value2 != 0) {
                            return value1 % value2;
                        } else {
                            errorHandler.flag(
                                    node,
                                    RuntimeErrorCode.DIVISION_BY_ZERO,
                                    this
                            );
                            return 0;
                        }
                    }

                }
            } else {
                // 至少一个是否float的  算术运算
                float value1 = operand1 instanceof Integer
                        ? (Integer) operand1
                        : (Float) operand1;
                float value2 = operand2 instanceof Integer
                        ? (Integer) operand2
                        : (Float) operand2;

                switch (nodeType) {
                    case ADD: {
                        return value1 + value2;
                    }
                    case SUBTRACT: {
                        return value1 - value2;
                    }
                    case MULTIPLY: {
                        return value1 * value2;
                    }
                    case FLOAT_DIVIDE: {
                        if (value2 != 0.0f) {
                            return value1 / value2;
                        } else {
                            errorHandler.flag(
                                    node,
                                    RuntimeErrorCode.DIVISION_BY_ZERO,
                                    this
                            );
                            return 0.0f;
                        }
                    }
                }
            }
        } else if ((nodeType == AND) || (nodeType == OR)) {
            // and 和 or
            boolean value1 = (boolean) operand1;
            boolean value2 = (boolean) operand2;

            switch (nodeType) {
                case AND: {
                    return value1 && value2;
                }
                case OR: {
                    return value1 || value2;
                }
            }
        } else if (integerMode) {
            //整数比较运算
            int value1 = (Integer) operand1;
            int value2 = (Integer) operand2;

            switch (nodeType) {
                case EQ:
                    return value1 == value2;
                case NE:
                    return value1 != value2;
                case LT:
                    return value1 < value2;
                case LE:
                    return value1 <= value2;
                case GT:
                    return value1 > value2;
                case GE:
                    return value1 >= value2;
            }
        } else {
            //浮点数比较运算
            float value1 = operand1 instanceof Integer
                    ? (Integer) operand1 : (Float) operand1;
            float value2 = operand2 instanceof Integer
                    ? (Integer) operand2 : (Float) operand2;

            switch (nodeType) {
                case EQ:
                    return value1 == value2;
                case NE:
                    return value1 != value2;
                case LT:
                    return value1 < value2;
                case LE:
                    return value1 <= value2;
                case GT:
                    return value1 > value2;
                case GE:
                    return value1 >= value2;
            }
        }
        return 0;
    }
}
