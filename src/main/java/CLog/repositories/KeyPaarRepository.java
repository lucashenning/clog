package CLog.repositories;

import CLog.entities.KeyPaar;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by Lucas on 11.05.2015.
 */
public interface KeyPaarRepository extends MongoRepository<KeyPaar, String> {

}