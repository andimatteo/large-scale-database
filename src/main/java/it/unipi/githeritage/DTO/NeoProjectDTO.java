package it.unipi.githeritage.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NeoProjectDTO {
    @Id
    private String id;
    private String owner;
    private String name;
}
