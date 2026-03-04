package com.tavinki.taskiu.modules.user.service.impl;

import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tavinki.taskiu.common.exceptions.EmailNotFoundException;
import com.tavinki.taskiu.common.exceptions.UserAlreadyExistsException;
import com.tavinki.taskiu.modules.user.entity.User;
import com.tavinki.taskiu.modules.user.repository.UserJpaRepository;
import com.tavinki.taskiu.modules.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServicePostgresImpl implements UserService {

    private final UserJpaRepository userRepository;

    @Override
    @Transactional
    public User createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        // Check if Email already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException(user.getEmail());
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElse(null);
    }

    @Override
    @Transactional
    public void updateUserEmail(String oldEmail, String newEmail) {
        // JPA update logic: Retrieve -> Modify -> Save (or auto-commit by Transaction)
        User user = userRepository.findByEmail(oldEmail)
                .orElseThrow(() -> new EmailNotFoundException("User with email " + oldEmail + " not found."));

        user.setEmail(newEmail);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateUser(@NonNull User user) {
        // Note: In JPA, if user is in detached state, save will execute merge
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUserByEmail(String email) {
        // To keep logic consistent with Mongo version: Find first then delete
        Optional<User> userOptional = userRepository.findByEmail(email);
        userOptional.ifPresent(userRepository::delete);

        // Or if Repository has deleteByEmail method, can call directly:
        // userRepository.deleteByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(@NonNull String userId) {
        // Assume ID is String type (UUID or String ID)
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    @Transactional
    public User markEmailAsVerified(@NonNull String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EmailNotFoundException("User with email " + email + " not found."));

        user.setVerified(true);

        // save method returns the updated entity
        return userRepository.save(user);
    }
}
