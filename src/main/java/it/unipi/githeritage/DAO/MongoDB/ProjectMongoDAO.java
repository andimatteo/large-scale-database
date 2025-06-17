package it.unipi.githeritage.DAO.MongoDB;

import it.unipi.githeritage.DTO.ContribDTO;
import it.unipi.githeritage.DTO.LeaderboardProjectDTO;
import it.unipi.githeritage.DTO.ProjectDTO;
import it.unipi.githeritage.DTO.UserActivityDistributionDTO;
import it.unipi.githeritage.Model.MongoDB.Project;
import it.unipi.githeritage.Repository.MongoDB.MongoProjectRepository;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators.Filter;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Repository
public class ProjectMongoDAO {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoProjectRepository repo;

    public UserActivityDistributionDTO getUserActivityDistribution(String username) {

        // 1. intervallo [start, now]
        Instant now   = Instant.now();
        Instant start = now.minus(364, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);

        // 2. project #1: filtro commits per autore e range
        ProjectionOperation filtCommits = Aggregation.project()
                .and(
                        Filter.filter("commits")
                                .as("c")
                                .by(BooleanOperators.And.and(
                                        ComparisonOperators.Eq.valueOf("c.author").equalToValue(username),
                                        ComparisonOperators.Gte.valueOf("c.timestamp").greaterThanEqualToValue(start),
                                        ComparisonOperators.Lte.valueOf("c.timestamp").lessThanEqualToValue(now)
                                ))
                ).as("commits");

        // 3. unwind
        UnwindOperation unwind = Aggregation.unwind("commits");

        // 4. project #2: estraggo il campo "day" formattando commits.timestamp in "YYYY-MM-DD"
        ProjectionOperation projectDay = Aggregation.project()
                // costruisce il campo day
                .and(DateOperators.DateToString.dateOf("commits.timestamp")
                        .toString("%Y-%m-%d"))
                .as("day");

        // 5. group per quel campo "day"
        GroupOperation group = Aggregation.group("day")
                .count().as("count");

        // 6. sort per _id (che è il day string)
        SortOperation sort = Aggregation.sort(Sort.by("_id").ascending());

        // 7. eseguo la pipeline
        Aggregation agg = Aggregation.newAggregation(
                filtCommits,
                unwind,
                projectDay,
                group,
                sort
        );
        AggregationResults<Document> results =
                mongoTemplate.aggregate(agg, "Projects", Document.class);

        // 8. preparo mappa di 365 giorni a zero
        Map<LocalDate,Integer> distribution = new LinkedHashMap<>();
        LocalDate today     = LocalDate.now();
        LocalDate startDate = today.minusDays(364);
        for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
            distribution.put(d, 0);
        }

        // 9. sovrascrivo con i conteggi ritornati
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Document doc : results.getMappedResults()) {
            String dayStr = doc.getString("_id");          // es. "2025-06-17"
            int    cnt    = doc.getInteger("count", 0);
            LocalDate day = LocalDate.parse(dayStr, fmt);
            distribution.put(day, cnt);
        }

        // 10. compilo e restituisco il DTO
        UserActivityDistributionDTO dto = new UserActivityDistributionDTO();
        dto.setDailyCommits(distribution);
        return dto;
    }

    public List<LeaderboardProjectDTO> getAllTimeLeaderboard() {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.unwind("comments"),
                Aggregation.group("_id")
                        .first("_id").as("projectId")
                        .first("name").as("name")
                        .avg("comments.stars").as("averageRating")
                        .count().as("commentCount"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "averageRating"))
        );
        return mongoTemplate
                .aggregate(agg, "Projects", LeaderboardProjectDTO.class)
                .getMappedResults();
    }

    public List<LeaderboardProjectDTO> getLeaderboardLastMonths(int months) {
        Instant cutoff = Instant.now().minus(months, ChronoUnit.MONTHS);
        Aggregation agg = Aggregation.newAggregation(
                // “srotola” l’array dei commenti
                Aggregation.unwind("comments"),
                // filtra per timestamp dei commenti
                Aggregation.match(Criteria.where("comments.timestamp").gte(cutoff)),
                Aggregation.group("_id")
                        .first("_id").as("projectId")
                        .first("name").as("name")
                        .avg("comments.stars").as("averageRating")
                        .count().as("commentCount"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "averageRating"))
        );
        return mongoTemplate
                .aggregate(agg, "Projects", LeaderboardProjectDTO.class)
                .getMappedResults();
    }

    public List<ContribDTO> getAllTimeContriboard() {
        Aggregation agg = Aggregation.newAggregation(
                // 1) “srotola” l’array commits
                Aggregation.unwind("commits"),
                // 2) raggruppa per commits.username, somma linesAdded
                Aggregation.group("commits.username")
                        .sum("commits.linesAdded").as("linesAdded"),
                // 3) proietta nei campi del DTO
                Aggregation.project("linesAdded")
                        .and("_id").as("username"),
                // 4) ordina decrescente
                Aggregation.sort(Sort.Direction.DESC, "linesAdded")
        );

        return mongoTemplate
                .aggregate(agg, "Projects", ContribDTO.class)
                .getMappedResults();
    }

    public List<ContribDTO> getLastMonthsContriboard(int months) {
        Date threshold = Date.from(Instant.now().minus(months, ChronoUnit.MONTHS));

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.unwind("commits"),
                // filtra solo i commit più recenti di threshold
                Aggregation.match(Criteria.where("commits.timestamp").gte(threshold)),
                Aggregation.group("commits.username")
                        .sum("commits.linesAdded").as("linesAdded"),
                Aggregation.project("linesAdded")
                        .and("_id").as("username"),
                Aggregation.sort(Sort.Direction.DESC, "linesAdded")
        );

        return mongoTemplate
                .aggregate(agg, "Projects", ContribDTO.class)
                .getMappedResults();
    }

    public List<ContribDTO> getAllTimeByProject(String projectId) {
        Aggregation agg = Aggregation.newAggregation(
                // 0) filtra il progetto
                Aggregation.match(Criteria.where("_id").is(projectId)),
                // 1) unwind commits
                Aggregation.unwind("commits"),
                // 2) group by commits.username
                Aggregation.group("commits.username")
                        .sum("commits.linesAdded").as("linesAdded"),
                // 3) project into DTO shape
                Aggregation.project("linesAdded")
                        .and("_id").as("username"),
                // 4) sort desc
                Aggregation.sort(Sort.Direction.DESC, "linesAdded")
        );
        return mongoTemplate
                .aggregate(agg, "Projects", ContribDTO.class)
                .getMappedResults();
    }


    public List<ContribDTO> getLastMonthsByProject(String projectId, int months) {
        Date threshold = Date.from(Instant.now().minus(months, ChronoUnit.MONTHS));
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("_id").is(projectId)),
                Aggregation.unwind("commits"),
                Aggregation.match(Criteria.where("commits.timestamp").gte(threshold)),
                Aggregation.group("commits.username")
                        .sum("commits.linesAdded").as("linesAdded"),
                Aggregation.project("linesAdded")
                        .and("_id").as("username"),
                Aggregation.sort(Sort.Direction.DESC, "linesAdded")
        );
        return mongoTemplate
                .aggregate(agg, "Projects", ContribDTO.class)
                .getMappedResults();
    }

    public Object save(Project project) {
        if (project.getCreationDate() == null) {
            project.setCreationDate(Instant.now());
        }
        return repo.save(project);
    }

    public ProjectDTO addProject(ProjectDTO projectDTO) {
        Project project = Project.fromDTO(projectDTO);
        if (project.getCreationDate() == null) {
            project.setCreationDate(Instant.now());
        }
        Project savedProject = repo.save(project);
        return ProjectDTO.fromProject(savedProject);
    }

    public ProjectDTO updateProject(ProjectDTO projectDTO) {
        // update existing project
        Project existingProject = repo.findById(projectDTO.getId())
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectDTO.getId()));
        existingProject.setName(projectDTO.getName());
        existingProject.setDescription(projectDTO.getDescription());
        existingProject.setOwner(projectDTO.getOwner());
        Project updatedProject = repo.save(existingProject);
        return ProjectDTO.fromProject(updatedProject);        
    }
}
