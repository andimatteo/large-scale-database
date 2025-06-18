package it.unipi.githeritage.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;

import it.unipi.githeritage.Model.MongoDB.Commit;
import it.unipi.githeritage.Model.MongoDB.Project;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectDTO {
    private String id;
    private String name;
    private String description;
    private String owner;
    private String version;
    private Instant creationDate;
    private List<String> administrators;
    private List<Commit> commits;
    private List<String> fileIds;

    public static ProjectDTO fromProject(Project savedProject) {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setId(savedProject.getId());
        projectDTO.setName(savedProject.getName());
        projectDTO.setDescription(savedProject.getDescription());
        projectDTO.setOwner(savedProject.getOwner());
        projectDTO.setVersion(savedProject.getVersion());
        projectDTO.setCreationDate(savedProject.getCreationDate());
        projectDTO.setAdministrators(savedProject.getAdministrators());
        projectDTO.setFileIds(savedProject.getFileIds());
        return projectDTO;
    }
}
