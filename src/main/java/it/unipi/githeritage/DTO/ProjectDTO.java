package it.unipi.githeritage.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.unipi.githeritage.Model.MongoDB.Comment;
import it.unipi.githeritage.Model.MongoDB.Commit;
import it.unipi.githeritage.Model.MongoDB.Project;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectDTO {

    @JsonProperty("_id")
    private String id;

    private String name;
    private String owner;
    private String description;
    private String version;
    private Instant creationDate;

    private List<String> administrators;
    private List<Comment> comments;
    private Integer commitsCount;
    private Integer filesCount;

    public static ProjectDTO fromProject(Project project) {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setId(project.getId());
        projectDTO.setName(project.getName());
        projectDTO.setDescription(project.getDescription());
        projectDTO.setOwner(project.getOwner());
        projectDTO.setVersion(project.getVersion());
        projectDTO.setCreationDate(project.getCreationDate());
        projectDTO.setAdministrators(project.getAdministrators());
        projectDTO.setComments(project.getComments());
        return projectDTO;
    }
}
