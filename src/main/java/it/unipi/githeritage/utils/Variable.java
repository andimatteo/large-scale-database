package it.unipi.githeritage.utils;

public class Variable {
    private final String variableName;
    private Object variableValue;

    public String getVariableName() {
        return variableName;
    }

    public Object getVariableValue() {
        return variableValue;
    }

    public void setVariableValue(Object variableValue) {
        this.variableValue = variableValue;
    }

    public Variable(String variableName, Object variableValue) {
        this.variableName = variableName;
        this.variableValue = variableValue;
    }
}
