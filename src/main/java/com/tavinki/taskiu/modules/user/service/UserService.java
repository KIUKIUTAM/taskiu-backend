package com.tavinki.taskiu.modules.user.service;

import com.tavinki.taskiu.modules.user.entity.User;
import org.springframework.lang.NonNull;

public interface UserService {

    /**
     * 創建新使用者
     * 
     * @param user 使用者實體
     * @return 創建後的使用者
     */
    User createUser(User user);

    /**
     * 透過 Email 獲取使用者
     * 
     * @param email 信箱
     * @return 使用者實體，若無則返回 null
     */
    User getUserByEmail(String email);

    /**
     * 更新使用者 Email
     * 
     * @param oldEmail 舊信箱
     * @param newEmail 新信箱
     */
    void updateUserEmail(String oldEmail, String newEmail);

    /**
     * 更新使用者資訊
     * 
     * @param user 使用者實體
     */
    void updateUser(@NonNull User user);

    /**
     * 透過 Email 刪除使用者
     * 
     * @param email 信箱
     */
    void deleteUserByEmail(String email);

    /**
     * 透過 ID 獲取使用者
     * 
     * @param userId 使用者 ID
     * @return 使用者實體
     */
    User getUserById(@NonNull String userId);

    /**
     * 標記 Email 為已驗證
     * 
     * @param email 信箱
     * @return 更新後的使用者實體
     */
    User markEmailAsVerified(@NonNull String email);
}
