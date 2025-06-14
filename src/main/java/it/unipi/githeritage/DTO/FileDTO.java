package it.unipi.githeritage.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import it.unipi.githeritage.Model.MongoDB.File;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileDTO {
    @JsonProperty("_id")
    private String id;

    private String projectId;
    private String path;
    private String type;
    private Integer size;
    private String content;
    private Set<String> classes;

    public static FileDTO fromEntity(File f) {
        FileDTO dto = new FileDTO();
        dto.setId(f.getId());
        dto.setProjectId(f.getProjectId());
        dto.setPath(f.getPath());
        dto.setType(f.getType());
        dto.setSize(f.getSize());
        dto.setContent(f.getContent());
        dto.setClasses(f.getClasses());
        return dto;
    }
}