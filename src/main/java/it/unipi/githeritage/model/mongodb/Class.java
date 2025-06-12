package it.unipi.githeritage.model.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

import java.util.List;
import java.util.Set;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Projects")
public class Class {
    @Id
    private String id;

    private String name;
    private Set<String> fields; // ??
    private Set<Method> methods;
}
