package it.unipi.githeritage.model.node4j;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import lombok.Data;

@Data
@Node
public class User {
    @Id
    private String id;
    private String username;

    // Outgoing "FOLLOWS" relationship: this user follows other users
    @Relationship(type = "FOLLOWS", direction = Relationship.Direction.OUTGOING)
    private java.util.Set<User> follows;

    // Outgoing "COLLABORATES_ON" relationship: this user collaborates on projects
    @Relationship(type = "COLLABORATES_ON", direction = Relationship.Direction.OUTGOING)
    private java.util.Set<Project> collaboratesOn;

}