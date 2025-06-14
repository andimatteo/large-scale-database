package it.unipi.githeritage.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ContribDTO extends BaseDTO {
    private String username;
    private long linesAdded;
}
