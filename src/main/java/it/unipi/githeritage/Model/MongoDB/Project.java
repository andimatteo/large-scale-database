package it.unipi.githeritage.Model.MongoDB;

import it.unipi.githeritage.DTO.ProjectDTO;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.time.Instant;
import java.util.List;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Projects")
@CompoundIndexes({
        @CompoundIndex(name = "username_projectName", def = "{ 'owner': 1, 'name': 1 }")
})
public class Project {

    @Id
    @JsonProperty("_id")
    private String id;

    private String name;
    private String description;
    private String owner;
    private String version;
    private Instant creationDate;
    private String packageName;

    private List<String> administrators;
    private List<String> fileIds;
    private List<String> commitIds;
    private List<Comment> comments;

    // todo non fare document embedding con i commit ma fare document linking
    // reference to commits

    public static ProjectDTO toDTO(Project project) {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setOwner(project.getOwner());
        dto.setVersion(project.getVersion());
        dto.setCreationDate(project.getCreationDate());
        dto.setAdministrators(project.getAdministrators());
        dto.setComments(project.getComments());
        dto.setCommitsCount(project.getCommitIds() != null ? project.getCommitIds().size() : 0);
        dto.setFilesCount(project.getFileIds() != null ? project.getFileIds().size() : 0);
        return dto;
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
        project.setComments(projectDTO.getComments());
        return project;
    }

    public boolean addAdministrator(String username){
        // if not already present
        if (administrators.contains(username))
            return false;
        return administrators.add(username);
    }

    public boolean removeAdministrator(String username){
        // if present
        if (!administrators.contains(username))
            return false;
//        System.out.println("Eliminating administrator " + username);
        return administrators.remove(username);
    }
}

