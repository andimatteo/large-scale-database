package it.unipi.githeritage.model.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Methods")
public class Method {

    @Id
    @JsonProperty("_id")
    private String id;

    private String name;
    private List<String> parameters;
    private String returnType;
    private String body;

    // campo probabilmente da rivedere
    private String description;
}
