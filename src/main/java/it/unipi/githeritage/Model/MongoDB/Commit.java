package it.unipi.githeritage.Model.MongoDB;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Commits")
public class Commit {
    @Id
    @JsonProperty("_id")
    private String id;

    private String author;          // ridondanza necessaria per fare grouping
    private Integer linesAdded;
    private Integer linesDeleted;
    private Integer filesModified;
    private Instant timestamp;

}
