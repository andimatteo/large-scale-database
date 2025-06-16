package it.unipi.githeritage.Service;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import it.unipi.githeritage.DAO.MongoDB.FileMongoDAO;
import it.unipi.githeritage.DTO.FileContentDTO;
import it.unipi.githeritage.DTO.FileDTO;
import it.unipi.githeritage.DTO.ProjectDTO;
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
    private MongoClient mongoClient;
    @Autowired
    private FileMongoDAO fileMongoDAO;
    @Autowired
    private ProjectService projectService;

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

    public FileDTO addFile(FileDTO fileDTO, String authenticatedUsername) {
        ClientSession session = mongoClient.startSession();
        try {
            session.startTransaction();

            // Validate project ownership
            ProjectDTO project = projectService.getProjectById(fileDTO.getProjectId());
            if (project == null) {
                throw new RuntimeException("Project not found with id: " + fileDTO.getProjectId());
            }

            if (!project.getAdministrators().contains(authenticatedUsername)) {
                throw new RuntimeException("User is not authorized to add files to this project.");
            }

            // Add the file
            FileDTO addedFile = fileMongoDAO.addFile(fileDTO);

            session.commitTransaction();
            return addedFile;

        } catch (Exception e) {
            session.abortTransaction();
            throw new RuntimeException("Failed to add file: " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }

    public FileDTO updateFile(FileDTO fileDTO, String authenticatedUsername) {
        ClientSession session = mongoClient.startSession();
        try {
            session.startTransaction();

            // First, get the existing file to check project ownership
            FileDTO existingFile = getFileMetadata(fileDTO.getId());
            if (existingFile == null) {
                throw new RuntimeException("File not found with id: " + fileDTO.getId());
            }

            // Validate project ownership
            ProjectDTO project = projectService.getProjectById(existingFile.getProjectId());
            if (project == null) {
                throw new RuntimeException("Project not found with id: " + existingFile.getProjectId());
            }

            if (!project.getAdministrators().contains(authenticatedUsername)) {
                throw new RuntimeException("User is not authorized to update files in this project.");
            }

            // Update the file
            FileDTO updatedFile = fileMongoDAO.updateFile(fileDTO);

            session.commitTransaction();
            return updatedFile;

        } catch (Exception e) {
            session.abortTransaction();
            throw new RuntimeException("Failed to update file: " + e.getMessage(), e);
        } finally {
            session.close();
        }
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
