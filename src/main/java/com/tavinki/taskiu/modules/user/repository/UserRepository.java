
package com.tavinki.taskiu.modules.user.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.tavinki.taskiu.modules.user.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    // 1. 方法命名查詢 (Derived Query Methods)
    Optional<User> findByEmail(String email);

    // 2. 自定義 JSON 查詢 (JSON Query)
    // ?0 代表第一個參數
    @Query("{ 'name' : ?0 }")
    List<User> findUsersByNameCustom(String name);

}
