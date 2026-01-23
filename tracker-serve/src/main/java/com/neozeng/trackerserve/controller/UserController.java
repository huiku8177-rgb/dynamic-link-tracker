package com.neozeng.trackerserve.controller;

import com.neozeng.trackerserve.common.Result;
import com.neozeng.trackerserve.exception.UnAuthorizedException;
import com.neozeng.trackerserve.pojo.User;
import com.neozeng.trackerserve.pojo.dto.UserInfoDTO;
import com.neozeng.trackerserve.util.UserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户信息", description = "用户信息查询相关接口")
public class UserController {

    /**
     * 获取当前登录用户的信息，用于前端刷新后恢复登录态
     */
    @Operation(summary = "获取当前用户信息", description = "根据当前登录态返回用户基础信息")
    @GetMapping("/info")
    public Result<UserInfoDTO> getCurrentUserInfo() {
        User user = UserHolder.getUser();
        if (user == null) {
            throw new UnAuthorizedException("登录状态已失效，请重新登录");
        }
        if (user.getId() != null && user.getId() == 0L) {
            // 游客模式不返回真实用户信息
            throw new UnAuthorizedException("游客模式下暂无账户信息，请注册后继续");
        }

        UserInfoDTO dto = new UserInfoDTO(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail()
        );
        return Result.success(dto);
    }
}


