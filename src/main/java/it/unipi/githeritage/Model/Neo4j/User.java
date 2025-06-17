package it.unipi.githeritage.Model.Neo4j;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import lombok.Data;

import java.util.Set;

@Data
@Node
public class User {

    @Id @Property("username")
    private String username;

    // Chi seguo
    @Relationship(type = "FOLLOWS", direction = Relationship.Direction.OUTGOING)
    private Set<User> follows;

    // Chi mi segue
    @Relationship(type = "FOLLOWS", direction = Relationship.Direction.INCOMING)
    private Set<User> followers;

    // Outgoing "COLLABORATES_ON" relationship: this user collaborates on projects
    @Relationship(type = "COLLABORATES_ON", direction = Relationship.Direction.OUTGOING)
    private Set<Project> collaboratesOn;

}