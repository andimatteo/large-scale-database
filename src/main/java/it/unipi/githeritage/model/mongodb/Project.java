package it.unipi.githeritage.model.mongodb;

import org.springframework.data.annotation.Id;

import it.unipi.githeritage.model.mongodb.File;
import java.time.Instant;
import java.util.List;

public class Project {

    @Id
    private String id;

    private String name;
    private String description;
    private String owner;
    private String version;
    private List<String> administrators;
    private List<Comment> comments;
    private Instant creationDate;
    private List<File> files;

    public Project(String id, String name, String description, String owner, String version, List<String> administrators, List<Comment> comments, Instant creationDate, List<File> files) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.version = version;
        this.administrators = administrators;
        this.comments = comments;
        this.creationDate = creationDate;
        this.files = files;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<String> getAdministrators() {
        return administrators;
    }

    public void setAdministrators(List<String> administrators) {
        this.administrators = administrators;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }
}

