package com.tavinki.taskiu.mongo.service;

import com.tavinki.taskiu.exception.UserAlreadyExistsException;
import com.tavinki.taskiu.mongo.entity.User;
import com.tavinki.taskiu.mongo.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (getUserByEmail(user.getEmail()) != null) {
            throw new UserAlreadyExistsException(user.getEmail());
        }
        return userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElse(null);
    }

    @Transactional
    public void updateUserEmail(String oldEmail, String newEmail) {
        User user = getUserByEmail(oldEmail);
        user.setEmail(newEmail);
        userRepository.save(user);
    }
}
