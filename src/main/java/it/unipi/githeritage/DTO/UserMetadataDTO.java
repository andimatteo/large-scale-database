package it.unipi.githeritage.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserMetadataDTO extends BaseDTO {
    @Id
    @JsonProperty("_id")
    private String username;
    private String email;
    private String name;
    private String surname;
    private Boolean isAdmin;
    private Instant registrationDate;
}
