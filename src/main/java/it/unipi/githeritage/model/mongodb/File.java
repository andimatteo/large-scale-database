package it.unipi.githeritage.model.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;

import java.util.List;

public class File {

    @Id
    private String id;

    private String path;
    private String type;
    private Integer size;
    private String content;
    private List<String> classes;
}
