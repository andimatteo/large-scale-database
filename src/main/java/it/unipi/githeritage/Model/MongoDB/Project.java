package it.unipi.githeritage.Model.MongoDB;

import it.unipi.githeritage.DTO.ProjectDTO;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Set;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Projects")
public class Project {

    @Id
    @JsonProperty("_id")
    private String id;

    private String name;
    private String description;
    private String owner;
    private String version;
    private Instant creationDate;
    private List<String> administrators;
    private Set<String> fileIds;

    // array of comments
    private List<Comment> comments;

    // array of commits
    private List<Commit> commits;

    public ProjectDTO toDTO() {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(this.id);
        dto.setName(this.name);
        dto.setDescription(this.description);
        dto.setOwner(this.owner);
        dto.setVersion(this.version);
        dto.setAdministrators(this.administrators);
        dto.setCreationDate(this.creationDate);
        return dto;
    }
}

