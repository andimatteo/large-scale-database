package it.unipi.githeritage.Model.MongoDB;

import it.unipi.githeritage.DTO.ProjectDTO;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.access.method.P;

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

    public ProjectDTO toDTO(Project project) {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setId(project.getId());
        projectDTO.setName(project.getName());
        projectDTO.setDescription(project.getDescription());
        projectDTO.setOwner(project.getOwner());
        projectDTO.setVersion(project.getVersion());
        projectDTO.setCreationDate(project.getCreationDate());
        projectDTO.setAdministrators(project.getAdministrators());
        return projectDTO;
    }

    public static Project fromDTO(ProjectDTO projectDTO) {
        Project project = new Project();
        project.setId(projectDTO.getId());
        project.setName(projectDTO.getName());
        project.setDescription(projectDTO.getDescription());
        project.setOwner(projectDTO.getOwner());
        project.setVersion(projectDTO.getVersion());
        project.setCreationDate(projectDTO.getCreationDate());
        project.setAdministrators(projectDTO.getAdministrators());
        return project;
    }
}

