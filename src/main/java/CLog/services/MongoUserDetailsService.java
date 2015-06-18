package CLog.services;

import CLog.entities.User;
import CLog.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Lucas on 18.06.2015.
 */
@Service
public class MongoUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    public User loadUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void createUser(User user ) {
        userRepository.save(user);
    }

    public void deleteUser(String username) {

    }

    public void updateUser(User user) {

    }

    public boolean userExists(String username) {
        return true;
    }

    public void changePassword(String oldPassword, String newPassword) {

    }

    public void addDefaultUser() {
        List<SimpleGrantedAuthority> auths = new java.util.ArrayList<SimpleGrantedAuthority>();
        auths.add(new SimpleGrantedAuthority("Admin"));
        User user = new User("user", "user", auths);
        userRepository.save(user);
    }

}
