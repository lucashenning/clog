package CLog.repositories;

import CLog.entities.KeyPaar;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

/**
 * Created by Lucas on 11.05.2015.
 */
public interface KeyPaarRepository extends MongoRepository<KeyPaar, String> {

    List<KeyPaar> findByTimestampBetween(Date startDate, Date endDate);

}
