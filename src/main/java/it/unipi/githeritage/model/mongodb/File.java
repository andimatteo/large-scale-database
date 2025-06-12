package it.unipi.githeritage.model.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.neo4j.core.schema.GeneratedValue;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

import java.util.List;
import java.util.Set;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Files")
public class File {

    @Id
    private String id;

    private String path;
    private String type;
    private Integer size;
    private String content;
    private Set<String> classes;
}
