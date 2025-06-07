package com.securelogwatcher.controller;

import com.securelogwatcher.domain.User;
import com.securelogwatcher.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final UserRepository userRepository;

    public TestController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // @GetMapping("/save")
    // public String saveUser() {
    // User user = new User("TestUser");
    // userRepository.save(user);
    // return "User saved with ID: " + user.getId();
    // }

    @PostMapping("/user")
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    @GetMapping("/list")
    public Iterable<User> listUsers() {
        return userRepository.findAll();
    }
}