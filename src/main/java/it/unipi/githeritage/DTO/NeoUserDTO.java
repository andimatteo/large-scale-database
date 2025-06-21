package it.unipi.githeritage.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NeoUserDTO {
    @Id
    private String id;
    private String username;
}
