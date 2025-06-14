package it.unipi.githeritage.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeaderboardProjectDTO {
    private String projectId;
    private String name;
    private double averageRating;
    private long commentCount;
}