package CLog.repositories;

import CLog.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by Lucas on 18.06.2015.
 */
public interface UserRepository extends MongoRepository<User, String> {
    User findByUsername(String username);
}
