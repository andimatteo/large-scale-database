package it.unipi.githeritage.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyCommitCountDTO {
    private String day;
    private int count;
}
