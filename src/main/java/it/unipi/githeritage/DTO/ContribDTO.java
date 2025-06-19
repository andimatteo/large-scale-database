package it.unipi.githeritage.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ContribDTO extends BaseDTO {
    private String username;
    private long linesAdded;
    private Instant lastContribution;
}
