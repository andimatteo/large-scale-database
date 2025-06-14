package it.unipi.githeritage.DTO;

import java.time.LocalDate;
import java.util.Map;

public class UserActivityDistributionDTO {
    private Map<LocalDate, Integer> dailyCommits;

    public Map<LocalDate, Integer> getDailyCommits() {
        return dailyCommits;
    }

    public void setDailyCommits(Map<LocalDate, Integer> dailyCommits) {
        this.dailyCommits = dailyCommits;
    }
}
