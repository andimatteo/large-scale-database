package it.unipi.githeritage.Model.MongoDB;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.time.Instant;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Commit {
    @Id
    @JsonProperty("_id")
    private String id;

    private Integer linesAdded;
    private Integer linesDeleted;
    private String username;
    private String commitHash;
    private String message;
    private Instant timestamp;
}
