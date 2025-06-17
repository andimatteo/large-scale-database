package it.unipi.githeritage.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import it.unipi.githeritage.Model.MongoDB.File;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileWrapperDTO {
    File file;
    String action;
}
