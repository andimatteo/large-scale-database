package it.unipi.githeritage.model.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;

import it.unipi.githeritage.model.mongodb.File;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Set;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Projects")
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
    private Set<File> files;
}

