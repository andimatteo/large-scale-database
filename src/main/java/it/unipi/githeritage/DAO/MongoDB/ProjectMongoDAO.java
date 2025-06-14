package it.unipi.githeritage.DAO.MongoDB;

import com.mongodb.client.MongoCollection;
import it.unipi.githeritage.DTO.ContribDTO;
import it.unipi.githeritage.DTO.LeaderboardProjectDTO;
import it.unipi.githeritage.DTO.UserActivityDistributionDTO;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Repository
public class ProjectMongoDAO {

    @Autowired
    private MongoTemplate mongoTemplate;

    public UserActivityDistributionDTO getUserActivityDistribution(String username) {
        List<Document> pipeline = List.of(
                new Document("$unwind", "$commits"),
                new Document("$match", new Document("commits.author", username)),
                new Document("$group", new Document("_id",
                        new Document("$dateTrunc", new Document("date", "$commits.timestamp")
                                .append("unit", "day")))
                        .append("count", new Document("$sum", 1))),
                new Document("$sort", new Document("_id", 1))
        );

        MongoCollection<Document> collection = mongoTemplate.getCollection("Projects");
        List<Document> results = collection.aggregate(pipeline).into(new ArrayList<>());

        Map<LocalDate, Integer> distribution = new LinkedHashMap<>();
        for (Document doc : results) {
            Date date = doc.getDate("_id");
            int count = ((Number) doc.get("count")).intValue();
            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            distribution.put(localDate, count);
        }

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
}
