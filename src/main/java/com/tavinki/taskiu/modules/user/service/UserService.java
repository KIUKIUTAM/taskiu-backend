package com.tavinki.taskiu.modules.user.service;

import org.springframework.lang.NonNull;

import com.tavinki.taskiu.modules.user.entity.User;

public interface UserService {

    /**
     * Create new user
     * 
     * @param user User entity
     * @return Created user
     */
    User createUser(User user);

    /**
     * Get user by Email
     * 
     * @param email Email address
     * @return User entity, or null if not found
     */
    User getUserByEmail(String email);

    /**
     * Update user Email
     * 
     * @param oldEmail Old email
     * @param newEmail New email
     */
    void updateUserEmail(String oldEmail, String newEmail);

    /**
     * Update user information
     * 
     * @param user User entity
     */
    void updateUser(@NonNull User user);

    /**
     * Delete user by Email
     * 
     * @param email Email address
     */
    void deleteUserByEmail(String email);

    /**
     * Get user by ID
     * 
     * @param userId User ID
     * @return User entity
     */
    User getUserById(@NonNull String userId);

    /**
     * Mark Email as verified
     * 
     * @param email Email address
     * @return Updated user entity
     */
    User markEmailAsVerified(@NonNull String email);
}
