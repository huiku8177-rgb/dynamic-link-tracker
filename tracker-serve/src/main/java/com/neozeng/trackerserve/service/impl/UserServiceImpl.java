package com.neozeng.trackerserve.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.neozeng.trackerserve.mapper.UserMapper;
import com.neozeng.trackerserve.pojo.User;
import com.neozeng.trackerserve.pojo.dto.LoginDTO;
import com.neozeng.trackerserve.pojo.dto.LoginResultDTO;
import com.neozeng.trackerserve.pojo.dto.RegisterDTO;
import com.neozeng.trackerserve.service.UserService;
import com.neozeng.trackerserve.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public User register(RegisterDTO registerDTO) {
        // 1. 检查用户名是否已存在
        if (userMapper.existsByUsername(registerDTO.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 检查邮箱是否已存在
        if (userMapper.existsByEmail(registerDTO.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 3. 创建新用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        // 使用 Hutool 的 BCrypt 加密密码
        user.setPassword(BCrypt.hashpw(registerDTO.getPassword()));
        user.setNickname(registerDTO.getNickname() != null ? registerDTO.getNickname() : registerDTO.getUsername());

        // 4. 保存到数据库
        User savedUser = userMapper.save(user);
        log.info("用户注册成功：username={}, email={}", savedUser.getUsername(), savedUser.getEmail());
        return savedUser;
    }

    @Override
    public LoginResultDTO login(LoginDTO loginDTO) {
        // 1. 根据用户名或邮箱查找用户
        User user = findByUsernameOrEmail(loginDTO.getUsernameOrEmail());
        if (user == null) {
            throw new RuntimeException("用户名或邮箱不存在");
        }

        // 2. 验证密码
        boolean passwordMatches = BCrypt.checkpw(loginDTO.getPassword(), user.getPassword());
        if (!passwordMatches) {
            throw new RuntimeException("密码错误");
        }

        // 3. 生成 JWT Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());

        // 4. 返回登录结果
        log.info("用户登录成功：username={}, userId={}", user.getUsername(), user.getId());
        return new LoginResultDTO(
                token,
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail()
        );
    }

    @Override
    public User findById(Long id) {
        return userMapper.findById(id).orElse(null);
    }

    @Override
    public User findByUsernameOrEmail(String usernameOrEmail) {
        // 先尝试按用户名查找
        Optional<User> userByUsername = userMapper.findByUsername(usernameOrEmail);
        if (userByUsername.isPresent()) {
            return userByUsername.get();
        }

        // 再尝试按邮箱查找
        Optional<User> userByEmail = userMapper.findByEmail(usernameOrEmail);
        return userByEmail.orElse(null);
    }
}

