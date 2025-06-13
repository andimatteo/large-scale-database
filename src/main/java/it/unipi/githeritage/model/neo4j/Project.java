package it.unipi.githeritage.model.neo4j;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import lombok.Data;

@Data
@Node
public class Project {
    @Id
    private String id;
    private String uri;

    // Outgoing "HAS_METHOD" relationship: methods used in this project
    @Relationship(type = "HAS_METHOD", direction = Relationship.Direction.OUTGOING)
    private java.util.Set<Method> methods;

    // Outgoing "DEPENDS_ON" relationship: projects that this project depends on
    @Relationship(type = "DEPENDS_ON", direction = Relationship.Direction.OUTGOING)
    private java.util.Set<Project> dependsOn;

}
