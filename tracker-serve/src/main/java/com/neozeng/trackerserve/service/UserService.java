package com.neozeng.trackerserve.service;

import com.neozeng.trackerserve.pojo.User;
import com.neozeng.trackerserve.pojo.dto.LoginDTO;
import com.neozeng.trackerserve.pojo.dto.LoginResultDTO;
import com.neozeng.trackerserve.pojo.dto.RegisterDTO;

/**
 * 用户服务接口
 */
public interface UserService {
    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 注册成功的用户信息
     */
    User register(RegisterDTO registerDTO);

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 登录结果（包含 Token）
     */
    LoginResultDTO login(LoginDTO loginDTO);

    /**
     * 根据 ID 查询用户
     *
     * @param id 用户 ID
     * @return 用户信息
     */
    User findById(Long id);

    /**
     * 根据用户名或邮箱查询用户
     *
     * @param usernameOrEmail 用户名或邮箱
     * @return 用户信息
     */
    User findByUsernameOrEmail(String usernameOrEmail);
}

