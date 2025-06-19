package it.unipi.githeritage.DAO.MongoDB;

import it.unipi.githeritage.Model.MongoDB.Commit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CommitMongoDAO {

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<Commit> getCommitsPaginated(String owner, String projectName, int page, int pageSize) {
        try {
            int skip = (page - 1) * pageSize;

            Aggregation aggregation = Aggregation.newAggregation(
                    // 1. Match per progetto
                    Aggregation.match(Criteria.where("owner").is(owner).and("name").is(projectName)),

                    // 2. Lookup dei commit referenziati in commitIds
                    Aggregation.lookup("Commits", "commitIds", "_id", "commits"),

                    // 3. Unwind dell'array di commit
                    Aggregation.unwind("commits"),

                    // 4. Replace root con il singolo commit
                    Aggregation.replaceRoot("commits"),

                    // 5. Ordinamento per timestamp decrescente
                    Aggregation.sort(Sort.by(Sort.Direction.DESC, "timestamp")),

                    // 6. Skip per la pagina corrente
                    Aggregation.skip(skip),

                    // 7. Limite per pagina
                    Aggregation.limit(pageSize)
            );

            AggregationResults<Commit> results =
                    mongoTemplate.aggregate(aggregation, "Projects", Commit.class);

            return results.getMappedResults();

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving paginated commits: " + e.getMessage(), e);
        }
    }

    public List<Commit> getCommitsPaginated(String projectId, int page, int pageSize) {
        try {
            int skip = (page - 1) * pageSize;

            Aggregation aggregation = Aggregation.newAggregation(
                    // 1. Match per progetto
                    Aggregation.match(Criteria.where("_id").is(projectId)),

                    // 2. Lookup dei commit referenziati in commitIds
                    Aggregation.lookup("Commits", "commitIds", "_id", "commits"),

                    // 3. Unwind dell'array di commit
                    Aggregation.unwind("commits"),

                    // 4. Replace root con il singolo commit
                    Aggregation.replaceRoot("commits"),

                    // 5. Ordinamento per timestamp decrescente
                    Aggregation.sort(Sort.by(Sort.Direction.DESC, "timestamp")),

                    // 6. Skip per la pagina corrente
                    Aggregation.skip(skip),

                    // 7. Limite per pagina
                    Aggregation.limit(pageSize)
            );

            AggregationResults<Commit> results =
                    mongoTemplate.aggregate(aggregation, "Projects", Commit.class);

            return results.getMappedResults();

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving paginated commits: " + e.getMessage(), e);
        }
    }
}
