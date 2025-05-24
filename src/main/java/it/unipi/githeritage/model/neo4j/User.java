package it.unipi.githeritage.model.neo4j;

import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

@Node("User")
public class User {
    @Id
    private final String name;

    @Relationship(type = "FOLLOW", direction = Relationship.Direction.OUTGOING)
    private List<User> projects = new ArrayList<>();

    @Relationship(type = "ADMINISTRATE", direction = Relationship.Direction.OUTGOING)
    private List<Project> starredUsers = new ArrayList<>();

    public User(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<User> getProjects() {
        return projects;
    }

    public void setProjects(List<User> projects) {
        this.projects = projects;
    }

    public List<Project> getStarredUsers() {
        return starredUsers;
    }

    public void setStarredUsers(List<Project> starredUsers) {
        this.starredUsers = starredUsers;
    }
}

