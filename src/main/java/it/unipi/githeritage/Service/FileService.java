package it.unipi.githeritage.Service;

import com.mongodb.client.MongoClient;
import it.unipi.githeritage.DTO.FileContentDTO;
import it.unipi.githeritage.DTO.FileDTO;
import it.unipi.githeritage.Model.MongoDB.File;
import it.unipi.githeritage.Repository.MongoDB.MongoFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FileService {

    @Autowired
    private MongoFileRepository mongoFileRepository;
    @Autowired
    private MongoClient mongo;

    public FileDTO getFileMetadata(String id) {
        return mongoFileRepository.findById(id)
                .map(this::mapToDTO)
                .orElse(null);
    }

    public FileContentDTO getFileContent(String id) {
        return mongoFileRepository.findById(id)
                .map(f -> new FileContentDTO(f.getContent()))
                .orElse(null);
    }

    public Optional<FileDTO> findByProjectIdAndPath(String projectId, String path) {
        return mongoFileRepository.findByProjectIdAndPath(projectId, path)
                .map(FileDTO::fromEntity);
    }

    private FileDTO mapToDTO(File f) {
        FileDTO dto = new FileDTO();
        dto.setId(f.getId());
        dto.setPath(f.getPath());
        dto.setType(f.getType());
        dto.setSize(f.getSize());
        dto.setClasses(f.getClasses());
        return dto;
    }

}
