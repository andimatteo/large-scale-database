package it.unipi.githeritage.model.mongodb;

import org.springframework.data.annotation.Id;

import java.util.List;

public class Method {

    @Id
    private String id;

    private String name;
    private List<String> parameters;
    private String returnType;
    private String body;

    // campo probabilmente da rivedere
    private String description
}
