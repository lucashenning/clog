package CLog.repositories;

import CLog.entities.Request;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

/**
 * Created by l.henning on 16.06.2015.
 */
public interface RequestRepository extends MongoRepository<Request, String> { }
