package com.project.UserService.service;

import com.project.UserService.model.User;
import com.project.UserService.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    UserRepo userRepo;

    public void addUser(User user){
        userRepo.save(user);
    }

    public User getUser(UUID userId){
        Optional<User> user = userRepo.findById(userId);
        return user.orElse(null);
    }

    public User getUserByUsername(String username){
        Optional<User> user = userRepo.findByUsername(username);
        return user.orElse(null);
    }
}
