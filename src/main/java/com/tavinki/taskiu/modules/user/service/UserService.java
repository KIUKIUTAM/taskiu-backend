package com.tavinki.taskiu.modules.user.service;

import com.mongodb.client.result.UpdateResult;
import com.tavinki.taskiu.common.exception.EmailNotFoundException;
import com.tavinki.taskiu.common.exception.UserAlreadyExistsException;
import com.tavinki.taskiu.modules.user.entity.User;
import com.tavinki.taskiu.modules.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final MongoTemplate mongoTemplate;

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

    public void updateUserEmail(String oldEmail, String newEmail) {
        Query query = new Query(Criteria.where("email").is(oldEmail));

        Update update = new Update().set("email", newEmail);

        UpdateResult result = mongoTemplate.updateFirst(query, update, User.class);

        if (result.getMatchedCount() == 0) {
            throw new EmailNotFoundException("User with email " + oldEmail + " not found.");
        }
    }

    public void updateUser(@NonNull User user) {
        userRepository.save(user);
    }

    public void deleteUserByEmail(String email) {
        User user = getUserByEmail(email);
        if (user != null) {
            userRepository.delete(user);
        }
    }

    public User getUserById(@NonNull String userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Transactional
    public User markEmailAsVerified(@NonNull String email) {
        Query query = new Query(Criteria.where("email").is(email));

        Update update = new Update().set("verified", true);

        // 設定選項：returnNew(true) 表示返回"更新後"的物件
        // 如果是 false (預設值)，則會返回更新前的舊資料
        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

        User updatedUser = mongoTemplate.findAndModify(query, update, options, User.class);

        if (updatedUser == null) {
            throw new EmailNotFoundException("User with email " + email + " not found.");
        }
        return updatedUser;
    }
}
