package CLog.services;

import CLog.entities.User;
import CLog.repositories.UserRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Created by l.henning on 19.06.2015.
 */
@Service
public class UserService {

    private static Log log = LogFactory.getLog(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void addDefaultUser() {
        User user = new User();
        user.setUsername("user");
        user.setPassword("user");
        User search = userRepository.findByUsername(user.getUsername());
        if (search == null) {
            userRepository.save(user);
            log.warn("================================== DEFAULT USER CREATED ==================================");
            log.warn("USER: "+user.getUsername());
            log.warn("PASSWORD: "+user.getPassword());
            log.warn("================================== DEFAULT USER CREATED ==================================");
        } else {
            log.warn("Default User bereits vorhanden. Nichts unternommen.");
        }
    }

    public User saveUser(User preUser) {
        if ( preUser.getId() != null && userRepository.exists(preUser.getId()) ) {
            return userRepository.save(preUser);
        } else {
            User user = new User();
            user.setUsername(preUser.getUsername());
            user.setPassword(preUser.getPassword());
            user.setFirstName(preUser.getFirstName());
            user.setLastName(preUser.getLastName());
            return userRepository.save(user);
        }
    }

    public User getOne(String id) {
        return userRepository.findOne(id);
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public void deleteUser(String id) {
        User c = userRepository.findOne(id);
        userRepository.delete(c);
    }

    public long count() {
        return userRepository.count();
    }

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

}
