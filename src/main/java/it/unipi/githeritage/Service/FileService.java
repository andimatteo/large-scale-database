package it.unipi.githeritage.Service;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import it.unipi.githeritage.DAO.MongoDB.FileMongoDAO;
import it.unipi.githeritage.DTO.FileContentDTO;
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

    public File getFile(String id) {
        return mongoFileRepository.findById(id)
                .orElse(null);
    }

    public File getFile(String username, String projectName, String path) {
        return mongoFileRepository.findByOwnerAndProjectNameAndPath(username,projectName,path)
                .orElse(null);
    }

    public FileContentDTO getFileContent(String id) {
        return mongoFileRepository.findById(id)
                .map(f -> new FileContentDTO(f.getContent()))
                .orElse(null);
    }

//    public File addFile(File File, String authenticatedUsername) {
//        ClientSession session = mongoClient.startSession();
//        try {
//            session.startTransaction();
//
//            // Validate project ownership
//            ProjectDTO project = projectService.getProjectById(File.getProjectId());
//            if (project == null) {
//                throw new RuntimeException("Project not found with id: " + File.getProjectId());
//            }
//
//            if (!project.getAdministrators().contains(authenticatedUsername)) {
//                throw new RuntimeException("User is not authorized to add files to this project.");
//            }
//
//            // Add the file
//            File addedFile = fileMongoDAO.addFile(File);
//
//            project.getFileIds().add(addedFile.getId());
//            projectService.updateProject(project, authenticatedUsername);
//
//            session.commitTransaction();
//            return addedFile;
//
//        } catch (Exception e) {
//            session.abortTransaction();
//            throw new RuntimeException("Failed to add file: " + e.getMessage(), e);
//        } finally {
//            session.close();
//        }
//    }

//    public File updateFile(File File, String authenticatedUsername) {
//        ClientSession session = mongoClient.startSession();
//        try {
//            session.startTransaction();
//
//            // First, get the existing file to check project ownership
//            File existingFile = getFile(File.getId());
//            if (existingFile == null) {
//                throw new RuntimeException("File not found with id: " + File.getId());
//            }
//
//            // Validate project ownership
//            ProjectDTO project = projectService.getProjectById(existingFile.getProjectId());
//            if (project == null) {
//                throw new RuntimeException("Project not found with id: " + existingFile.getProjectId());
//            }
//
//            if (!project.getAdministrators().contains(authenticatedUsername)) {
//                throw new RuntimeException("User is not authorized to update files in this project.");
//            }
//
//            // Update the file
//            File updatedFile = fileMongoDAO.updateFile(File);
//
//            session.commitTransaction();
//            return updatedFile;
//
//        } catch (Exception e) {
//            session.abortTransaction();
//            throw new RuntimeException("Failed to update file: " + e.getMessage(), e);
//        } finally {
//            session.close();
//        }
//    }

//    public void deleteFile(String fileId, String authenticatedUsername) {
//        ClientSession session = mongoClient.startSession();
//        try {
//            session.startTransaction();
//
//            // First, get the existing file to check project ownership
//            File existingFile = getFile(fileId);
//            if (existingFile == null) {
//                throw new RuntimeException("File not found with id: " + fileId);
//            }
//
//            // Validate project ownership
//            ProjectDTO project = projectService.getProjectById(existingFile.getProjectId());
//            if (project == null) {
//                throw new RuntimeException("Project not found with id: " + existingFile.getProjectId());
//            }
//
//            if (!project.getAdministrators().contains(authenticatedUsername)) {
//                throw new RuntimeException("User is not authorized to delete files in this project.");
//            }
//
//            // Delete the file
//            fileMongoDAO.deleteFile(fileId);
//
//            // TODO delete also methods and also in neo4j
//
//            // Remove file ID from project
//            if (project.getFileIds() != null) {
//                project.getFileIds().remove(fileId);
//                projectService.updateProject(project, authenticatedUsername);
//            }
//
//            session.commitTransaction();
//
//        } catch (Exception e) {
//            session.abortTransaction();
//            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
//        } finally {
//            session.close();
//        }
//    }

//    public void deleteFile(String projectId, String path, String authenticatedUsername) {
//        ClientSession session = mongoClient.startSession();
//        try {
//            session.startTransaction();
//
//            // Find the file by projectId and path
//            Optional<File> fileOpt = findByProjectIdAndPath(projectId, path);
//            if (fileOpt.isEmpty()) {
//                throw new RuntimeException("File not found with projectId: " + projectId + " and path: " + path);
//            }
//
//            File existingFile = fileOpt.get();
//
//            // Validate project ownership
//            ProjectDTO project = projectService.getProjectById(projectId);
//            if (project == null) {
//                throw new RuntimeException("Project not found with id: " + projectId);
//            }
//
//            if (!project.getAdministrators().contains(authenticatedUsername)) {
//                throw new RuntimeException("User is not authorized to delete files in this project.");
//            }
//
//            // Delete the file
//            fileMongoDAO.deleteFile(existingFile.getId());
//
//            // TODO delete also methods and also in neo4j
//
//            // Remove file ID from project
//            if (project.getFileIds() != null) {
//                project.getFileIds().remove(existingFile.getId());
//                projectService.updateProject(project, authenticatedUsername);
//            }
//
//            session.commitTransaction();
//
//        } catch (Exception e) {
//            session.abortTransaction();
//            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
//        } finally {
//            session.close();
//        }
//    }

}
