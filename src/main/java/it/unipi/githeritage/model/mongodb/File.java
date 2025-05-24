package it.unipi.githeritage.model.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;

public class File {
    @Id
    @GeneratedValue
    private final String id;

    private final String filename;

    private final String content;

    public String getContent() {
        return content;
    }

    public String getFilename() {
        return filename;
    }

    public String getId() {
        return id;
    }

    public File(String id, String filename, String content) {
        this.id = id;
        this.filename = filename;
        this.content = content;
    }

    @Override
    public String toString() {
        return "File{" +
                "id='" + id + '\'' +
                ", filename='" + filename + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
