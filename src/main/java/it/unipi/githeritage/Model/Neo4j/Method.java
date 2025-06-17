package it.unipi.githeritage.Model.Neo4j;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import lombok.Data;

import java.util.Set;

@Data
@Node
public class Method {
    @Id
    private String id;

    private String username;
    private String fullyQualifiedName;
    private String methodName;

    private float hotness;
    
//    private String projectId; // ridondanze, da valutare se utile

    // fan in e out non credo serva davvero (tanto è già contenuto in hotness l'indice), per vedere quali chiamo uso il grafo

    // outgoing "CALLS" relationship: methods that this method calls
    @Relationship(type = "CALLS", direction = Relationship.Direction.OUTGOING)
    private Set<Method> is_called;

    @Relationship(type = "CALLS", direction = Relationship.Direction.INCOMING)
    private Set<Method> calls;
}
