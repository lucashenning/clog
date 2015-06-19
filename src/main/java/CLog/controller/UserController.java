package CLog.controller;

import CLog.entities.Request;
import CLog.entities.User;
import CLog.services.MongoUserDetailsService;
import CLog.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by l.henning on 19.06.2015.
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping(value="/count", method = RequestMethod.GET)
    public long count() {
        return userService.count();
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<User> getAll() {
        return userService.getAll();
    }

    // get request by id (Path)
    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    public User getOnebyPath(@PathVariable String id) {
        return userService.getOne(id);
    }


    @RequestMapping(method = RequestMethod.POST)
    public void add(@RequestBody User newuser) {
        userService.saveUser(newuser); // Wandelt den neuen (unvollständigen) Request um und schickt den vollständigen zurück.
    }
    @RequestMapping(value="/{id}", method = RequestMethod.POST)
    public void save(@RequestBody User user) {
        userService.saveUser(user); // Speichert den Request
    }

    @RequestMapping(value="/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable String id) {
        userService.deleteUser(id);
    }


}
