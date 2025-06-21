package it.unipi.githeritage.DAO.MongoDB;

import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import io.netty.channel.unix.RawUnixChannelOption;
import it.unipi.githeritage.DTO.*;
import it.unipi.githeritage.Model.MongoDB.Project;
import it.unipi.githeritage.Model.MongoDB.User;
import it.unipi.githeritage.Repository.MongoDB.MongoProjectRepository;
import it.unipi.githeritage.Repository.MongoDB.MongoUserRepository;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators.Filter;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ProjectMongoDAO {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoProjectRepository repo;
    @Autowired
    private MongoUserRepository mongoUserRepository;

    public List<DailyCommitCountDTO> getUserDailyActivity(String username){
        try {
            // check if user exists
            if (!mongoUserRepository.existsById(username)) {
                throw new RuntimeException("User not found");
            }

            Instant now = Instant.now();
            Instant start = now.minus(364, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);

            AggregationOptions options = AggregationOptions.builder()
                    .allowDiskUse(true)
                    .readConcern(ReadConcern.LOCAL)
                    .readPreference(ReadPreference.nearest())
                    .build();

            Aggregation aggregation = Aggregation.newAggregation(
                    User.class,
                    Aggregation.match(Criteria.where("_id").is(username)),
                    Aggregation.lookup("Commits", "commitIds", "_id", "commits"),
                    Aggregation.unwind("commits"),
                    Aggregation.match(
                            Criteria.where("commits.timestamp").gte(start).lte(now)
                    ),
                    Aggregation.project()
                            .andExpression("{$dateToString: {format: '%Y-%m-%d', date: '$commits.timestamp'}}")
                            .as("day"),
                    Aggregation.group("day").count().as("count"),
                    Aggregation.project("count").and("day").previousOperation(),
                    Aggregation.sort(Sort.by(Sort.Direction.ASC, "day"))
            )
                    .withOptions(options);

            // obtained results but days may be missing
            AggregationResults<DailyCommitCountDTO> results =
                    mongoTemplate.aggregate(aggregation, User.class, DailyCommitCountDTO.class);

            // fill all days
            Map<LocalDate, Integer> counts = results.getMappedResults().stream()
                    .collect(Collectors.toMap(
                            r -> LocalDate.parse(r.getDay()),  // day in formato "YYYY-MM-DD"
                            DailyCommitCountDTO::getCount));


            List<DailyCommitCountDTO> complete = new ArrayList<>(365);
            LocalDate cursor = start.atZone(ZoneOffset.UTC).toLocalDate();  // oppure ZoneId.of("Europe/Rome")
            LocalDate end    = now.atZone(ZoneOffset.UTC).toLocalDate();

            while (!cursor.isAfter(end)) {
                int c = counts.getOrDefault(cursor, 0);              // 0 se mancante
                complete.add(new DailyCommitCountDTO(cursor.toString(), c));
                cursor = cursor.plusDays(1);
            }

            return complete;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<LeaderboardProjectDTO> getAllTimeLeaderboard() {
        AggregationOptions options = AggregationOptions.builder()
                .allowDiskUse(true)
                .readConcern(ReadConcern.LOCAL)
                .readPreference(ReadPreference.nearest())
                .build();

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.unwind("comments"),
                Aggregation.group("_id")
                        .first("_id").as("projectId")
                        .first("name").as("name")
                        .avg("comments.stars").as("averageRating")
                        .count().as("commentCount"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "averageRating"))
        )
                .withOptions(options);
        return mongoTemplate
                .aggregate(agg, "Projects", LeaderboardProjectDTO.class)
                .getMappedResults();
    }

    public List<LeaderboardProjectDTO> getLeaderboardLastMonths(int months) {
        ZoneId zone = ZoneOffset.UTC;
        Instant cutoff = ZonedDateTime.now(zone)
                .minusMonths(months)
                .truncatedTo(ChronoUnit.DAYS)
                .toInstant();

//        System.out.println("cutoff: " + cutoff);

        AggregationOptions options = AggregationOptions.builder()
                .allowDiskUse(true)
                .readConcern(ReadConcern.LOCAL)
                .readPreference(ReadPreference.nearest())
                .build();

        Aggregation agg = Aggregation.newAggregation(
                // unwind dei commenti
                Aggregation.unwind("comments"),
                // filtra per timestamp dei commenti (anticipare il piu' possibile le match)
                Aggregation.match(Criteria.where("comments.timestamp").gte(cutoff)),
                Aggregation.group("_id")
                        .first("_id").as("projectId")
                        .first("name").as("name")
                        .avg("comments.stars").as("averageRating")
                        .count().as("commentCount"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "averageRating"))
        )
                .withOptions(options);
        return mongoTemplate
                .aggregate(agg, "Projects", LeaderboardProjectDTO.class)
                .getMappedResults();
    }

    public List<ContribDTO> getAllTimeContriboard() {

        AggregationOptions options = AggregationOptions.builder()
                .allowDiskUse(true)
                .readConcern(ReadConcern.LOCAL)
                .readPreference(ReadPreference.nearest())
                .build();


        Aggregation agg = Aggregation.newAggregation(
                // 1. Group by author
                Aggregation.group("author")
                        .sum("linesAdded").as("linesAdded")
                        .max("timestamp").as("lastContribution"),
                // 2. Project fields
                Aggregation.project("linesAdded", "lastContribution")
                        .and("_id").as("username"),
                // 3. Sort descending
                Aggregation.sort(Sort.Direction.DESC, "linesAdded"),
                // 4. Limit to top 100
                Aggregation.limit(100)
        )
                .withOptions(options);

        return mongoTemplate
                .aggregate(agg, "Commits", ContribDTO.class)
                .getMappedResults();
    }

    public List<ContribDTO> getLastMonthsContriboard(int months) {
        ZoneId zone = ZoneOffset.UTC;
        Instant threshold = ZonedDateTime.now(zone)
                .minusMonths(months)
                .truncatedTo(ChronoUnit.DAYS)
                .toInstant();

        AggregationOptions options = AggregationOptions.builder()
                .allowDiskUse(true)
                .readConcern(ReadConcern.LOCAL)
                .readPreference(ReadPreference.nearest())
                .build();

        Aggregation agg = Aggregation.newAggregation(
                // 1. filtri sui commit recenti
                Aggregation.match(Criteria.where("timestamp").gte(threshold)),
                // 2. Group by author
                Aggregation.group("author")
                        .sum("linesAdded").as("linesAdded")
                        .max("timestamp").as("lastContribution"),
                // 3. Project
                Aggregation.project("linesAdded", "lastContribution")
                        .and("_id").as("username"),
                // 4. Sort + limit
                Aggregation.sort(Sort.Direction.DESC, "linesAdded"),
                Aggregation.limit(100)
        )
                .withOptions(options);

        return mongoTemplate
                .aggregate(agg, "Commits", ContribDTO.class)
                .getMappedResults();
    }

    public List<ContribDTO> getAllTimeByProject(String projectId, int months) {
        List<AggregationOperation> pipeline = new ArrayList<>();

        // 1. Match progetto
        pipeline.add(Aggregation.match(Criteria.where("_id").is(projectId)));

        // 2. Lookup dei commit
        pipeline.add(Aggregation.lookup("Commits", "commitIds", "_id", "commitList"));

        // 3. Unwind
        pipeline.add(Aggregation.unwind("commitList"));

        // 4. (Opzionale) Filtro temporale sui commit
        if (months > 0) {
            ZoneId zone = ZoneOffset.UTC;
            Instant threshold = ZonedDateTime.now(zone)
                    .minusMonths(months)
                    .truncatedTo(ChronoUnit.DAYS)
                    .toInstant();
            pipeline.add(Aggregation.match(Criteria.where("commitList.timestamp").gte(threshold)));
        }

        // 5. Group per autore
        pipeline.add(Aggregation.group("commitList.author")
                .sum("commitList.linesAdded").as("linesAdded")
                .max("commitList.timestamp").as("lastContribution"));

        // 6. Project
        pipeline.add(Aggregation.project("linesAdded", "lastContribution")
                .and("_id").as("username"));

        // 7. Ordinamento e limite
        pipeline.add(Aggregation.sort(Sort.Direction.DESC, "linesAdded"));
        pipeline.add(Aggregation.limit(100));

        Aggregation agg = Aggregation.newAggregation(pipeline);

        return mongoTemplate
                .aggregate(agg, "Projects", ContribDTO.class)
                .getMappedResults();
    }

    public List<ContribDTO> getAllTimeByProject(String owner, String projectName, int months) {
        List<AggregationOperation> pipeline = new ArrayList<>();

        // 1. Match progetto
        pipeline.add(Aggregation.match(Criteria.where("owner").is(owner).and("name").is(projectName)));

        // 2. Lookup dei commit
        pipeline.add(Aggregation.lookup("Commits", "commitIds", "_id", "commitList"));

        // 3. Unwind
        pipeline.add(Aggregation.unwind("commitList"));

        // 4. (Opzionale) Filtro temporale sui commit
        if (months > 0) {
            ZoneId zone = ZoneOffset.UTC;
            Instant threshold = ZonedDateTime.now(zone)
                    .minusMonths(months)
                    .truncatedTo(ChronoUnit.DAYS)
                    .toInstant();
            pipeline.add(Aggregation.match(Criteria.where("commitList.timestamp").gte(threshold)));
        }

        // 5. Group per autore
        pipeline.add(Aggregation.group("commitList.author")
                .sum("commitList.linesAdded").as("linesAdded")
                .max("commitList.timestamp").as("lastContribution"));

        // 6. Project
        pipeline.add(Aggregation.project("linesAdded", "lastContribution")
                .and("_id").as("username"));

        // 7. Ordinamento e limite
        pipeline.add(Aggregation.sort(Sort.Direction.DESC, "linesAdded"));
        pipeline.add(Aggregation.limit(100));

        Aggregation agg = Aggregation.newAggregation(pipeline);

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
