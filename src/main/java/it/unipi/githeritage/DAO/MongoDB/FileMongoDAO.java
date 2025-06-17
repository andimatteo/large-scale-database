package it.unipi.githeritage.DAO.MongoDB;

import it.unipi.githeritage.DTO.FileDTO;
import it.unipi.githeritage.Model.MongoDB.File;
import it.unipi.githeritage.Repository.MongoDB.MongoFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class FileMongoDAO {

    @Autowired
    private MongoFileRepository mongoFileRepository;

    public FileDTO addFile(FileDTO fileDTO) {
        File file = new File();
        file.setProjectId(fileDTO.getProjectId());
        file.setPath(fileDTO.getPath());
        file.setType(fileDTO.getType());
        file.setSize(fileDTO.getSize());
        file.setContent(fileDTO.getContent());
        file.setClasses(fileDTO.getClasses());
        
        File savedFile = mongoFileRepository.save(file);
        return FileDTO.fromEntity(savedFile);
    }

    public FileDTO updateFile(FileDTO fileDTO) {
        File existingFile = mongoFileRepository.findById(fileDTO.getId())
                .orElseThrow(() -> new RuntimeException("File not found with id: " + fileDTO.getId()));
        
        // Update only the fields that are provided
        if (fileDTO.getPath() != null) {
            existingFile.setPath(fileDTO.getPath());
        }
        if (fileDTO.getType() != null) {
            existingFile.setType(fileDTO.getType());
        }
        if (fileDTO.getSize() != null) {
            existingFile.setSize(fileDTO.getSize());
        }
        if (fileDTO.getContent() != null) {
            existingFile.setContent(fileDTO.getContent());
        }
        if (fileDTO.getClasses() != null) {
            existingFile.setClasses(fileDTO.getClasses());
        }
        
        File updatedFile = mongoFileRepository.save(existingFile);
        return FileDTO.fromEntity(updatedFile);
    }

    public void deleteFile(String fileId) {
        if (!mongoFileRepository.existsById(fileId)) {
            throw new RuntimeException("File not found with id: " + fileId);
        }
        mongoFileRepository.deleteById(fileId);
    }
}
