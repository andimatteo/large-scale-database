package it.unipi.githeritage.model.mongodb;

import org.springframework.data.annotation.Id;

import java.util.List;

public class Class {
    @Id
    private String id;

    private String name;
    private List<String> fields;
    private List<Method> methods;
}
