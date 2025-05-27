package it.unipi.githeritage.model.mongodb;

import org.springframework.data.annotation.Id;

import java.util.List;

public class Class {
    @Id
    private String id;

    private String name;
    private List<String> fields;
    private List<Method> methods;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public List<Method> getMethods() {
        return methods;
    }

    public void setMethods(List<Method> methods) {
        this.methods = methods;
    }
}
