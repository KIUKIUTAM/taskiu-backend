package com.tavinki.taskiu.modules.user.service.impl;

import com.tavinki.taskiu.common.exception.EmailNotFoundException;
import com.tavinki.taskiu.common.exception.UserAlreadyExistsException;
import com.tavinki.taskiu.modules.user.entity.User;
import com.tavinki.taskiu.modules.user.repository.UserJpaRepository;
import com.tavinki.taskiu.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Profile("postgres")
@RequiredArgsConstructor
public class UserServicePostgresImpl implements UserService {

    private final UserJpaRepository userRepository;

    @Override
    @Transactional
    public User createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        // 檢查 Email 是否已存在
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
        // JPA 更新邏輯：先取出 -> 修改 -> 儲存(或由 Transaction 自動提交)
        User user = userRepository.findByEmail(oldEmail)
                .orElseThrow(() -> new EmailNotFoundException("User with email " + oldEmail + " not found."));

        user.setEmail(newEmail);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateUser(@NonNull User user) {
        // 注意：在 JPA 中，如果 user 是 detached 狀態，save 會執行 merge
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUserByEmail(String email) {
        // 為了保持與 Mongo 版本邏輯一致：先查再刪
        Optional<User> userOptional = userRepository.findByEmail(email);
        userOptional.ifPresent(userRepository::delete);

        // 或者如果 Repository 有 deleteByEmail 方法，也可以直接呼叫：
        // userRepository.deleteByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(@NonNull String userId) {
        // 假設 ID 是 String 類型 (UUID 或 String ID)
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    @Transactional
    public User markEmailAsVerified(@NonNull String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EmailNotFoundException("User with email " + email + " not found."));

        user.setVerified(true);

        // save 方法會返回更新後的實體
        return userRepository.save(user);
    }
}
