package com.staircase13.apperta.api;

import com.staircase13.apperta.service.UserService;
import com.staircase13.apperta.service.dto.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// TODO: this is temporary - it should be removed
@RestController
@RequestMapping("/api/users")
public class UsersEndpoint {

    private final UserService userService;

    @Autowired
    public UsersEndpoint(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(produces = "application/json")
    public List<UserDto> users() {
        return userService.getUsers();
    }

}
