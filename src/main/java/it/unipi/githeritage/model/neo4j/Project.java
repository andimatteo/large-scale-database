package it.unipi.githeritage.model.neo4j;

import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

@Node("Project")
public class Project {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
}