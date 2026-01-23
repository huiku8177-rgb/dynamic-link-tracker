package com.neozeng.trackerserve.mapper;

import com.neozeng.trackerserve.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author strive_qin
 * @version 1.0
 * @description UserMapper
 * @date 2026/1/18
 */
@Repository
public interface UserMapper extends JpaRepository<User, Long> {
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
}

