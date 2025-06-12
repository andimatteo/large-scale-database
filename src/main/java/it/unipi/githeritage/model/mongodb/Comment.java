package it.unipi.githeritage.model.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

import java.time.Instant;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Projects")
public class Comment {
    @Id
    private String id;

    private String authorId;
    private String authorName;
    private Integer stars;
    private String text;
    private Instant timestamp;
}
