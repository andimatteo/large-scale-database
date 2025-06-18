package it.unipi.githeritage.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PathDTO extends BaseDTO {
    private int distance;
    private List<String> nodes;
}
