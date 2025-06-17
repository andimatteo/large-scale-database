package it.unipi.githeritage.Model.MongoDB;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Commit {
    @Id
    @JsonProperty("_id")
    private String id;

    @Indexed(name = "idx_commits_author")
    private String author;

    private Integer linesAdded;
    private Integer linesDeleted;
    private Integer filesModified;
    private Instant timestamp;

}
