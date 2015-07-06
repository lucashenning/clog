package CLog.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * Created by l.henning on 18.06.2015.
 */
@RestController
@RequestMapping("/user")
public class LoginController {
    @RequestMapping(method = RequestMethod.GET)
    public Principal user(Principal user) {
        return user;
    } // returns an user object which is used by angularJS to determine if a login is successful
}
