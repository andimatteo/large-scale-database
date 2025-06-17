package it.unipi.githeritage.Model.MongoDB;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.time.Instant;
import java.util.Set;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Files")
@CompoundIndexes({
        @CompoundIndex(name = "owner_projectName_path_idx", def = "{ 'owner': 1, 'projectName': 1, 'path': 1 }")
})
public class File {

    @Id
    @JsonProperty("_id")
    private String id;

    // redundant fields to support index, not join operations
    private String owner;
    private String projectName;

    private String path;
    private String type;
    private Integer size;
    private Integer lines;
    private Instant lastModified;
    private String lastModifiedBy;
    private String content;

}
