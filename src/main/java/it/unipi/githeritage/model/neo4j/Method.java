package it.unipi.githeritage.model.node4j;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import lombok.Data;

@Data
@Node
public class Method {
    @Id
    private String id;

    private float hotness;
    
    private String projectId; // ridondanze, da valutare se utile

    // fan in e out non credo serva davvero (tanto è già contenuto in hotness l'indice), per vedere quali hciamo uso il grafo

    // outgoing "CALLS" relationship: methods that this method calls
    @Relationship(type = "CALLS", direction = Relationship.Direction.OUTGOING)
    private java.util.Set<Method> calls;
}
