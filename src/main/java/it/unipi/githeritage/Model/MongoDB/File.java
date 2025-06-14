package it.unipi.githeritage.Model.MongoDB;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.Set;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Files")
@CompoundIndexes({
        @CompoundIndex(name = "projectId_name_idx", def = "{'projectId': 1, 'path': 1}")
})
// compound index that supports both queries on:
//      1. projectId and path
//      2. projectId alone
public class File {

    @Id
    @JsonProperty("_id")
    private String id;

    private String projectId;   // id of the project
    private String path;        // absolute path of the file

    private String type;
    private Integer size;
    private String content;
    private Set<String> classes;
}
