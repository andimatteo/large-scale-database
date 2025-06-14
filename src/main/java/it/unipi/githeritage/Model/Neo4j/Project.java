package it.unipi.githeritage.Model.Neo4j;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import lombok.Data;

import java.util.Set;

@Data
@Node
public class Project {
    @Id
    private String id;
    private String name;

    // Outgoing "HAS_METHOD" relationship: methods used in this project
    @Relationship(type = "HAS_METHOD", direction = Relationship.Direction.OUTGOING)
    private Set<Method> methods;

    // Outgoing "DEPENDS_ON" relationship: projects that this project depends on
    @Relationship(type = "DEPENDS_ON", direction = Relationship.Direction.OUTGOING)
    private Set<Project> dependsOn;

}
