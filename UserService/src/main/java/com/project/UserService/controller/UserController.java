package com.project.UserService.controller;

import com.project.UserService.model.User;
import com.project.UserService.service.JwtService;
import com.project.UserService.service.UserService;
import org.apache.coyote.Response;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;

    @Autowired
    JwtService jwtService;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    PasswordEncoder passwordEncoder;

    @PostMapping("signin")
    public ResponseEntity<User> addUser(@RequestBody User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.addUser(user);
        User savedUser = userService.getUser(user.getUserId());
        if(savedUser == null){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("login")
    public String login(@RequestBody User user){
        UUID userId = userService.getUserByUsername(user.getUsername()).getUserId();
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        if(authentication.isAuthenticated())
            return jwtService.generateToken(userId);
        else
            return "Login Failed";
    }

    @GetMapping("get")
    public ResponseEntity<User> getUser(@RequestHeader(value = "X-User-Id", required = true, defaultValue = "defaultVal") String userId){
        User user = userService.getUser(UUID.fromString(userId));
        if(user == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }
}
