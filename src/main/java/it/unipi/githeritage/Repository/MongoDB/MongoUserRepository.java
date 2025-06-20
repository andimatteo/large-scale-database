package it.unipi.githeritage.Repository.MongoDB;

import it.unipi.githeritage.DTO.UserDTO;
import it.unipi.githeritage.DTO.UserMetadataDTO;
import it.unipi.githeritage.Model.MongoDB.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MongoUserRepository extends MongoRepository<User, String> {
    Optional<UserDTO> findByUsername(String username);
    Optional<Long> deleteByUsername(String username);
    Optional<List<UserMetadataDTO>> findTop100ByOrderByUsernameAsc();
    Page<UserMetadataDTO> findAllOrderByUsername(Pageable pageable);
}
