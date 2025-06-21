package it.unipi.githeritage.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.Neo4jClient;

@Configuration
public class Neo4jConfig {

    @Bean
    public Neo4jClient neo4jClient(org.neo4j.driver.Driver driver) {
        return Neo4jClient.create(driver);
    }
}
