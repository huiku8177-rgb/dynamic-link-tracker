package com.neozeng.trackerserve.controller;

import com.neozeng.trackerserve.common.Result;
import com.neozeng.trackerserve.pojo.dto.LoginDTO;
import com.neozeng.trackerserve.pojo.dto.LoginResultDTO;
import com.neozeng.trackerserve.pojo.dto.RegisterDTO;
import com.neozeng.trackerserve.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理用户注册和登录
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户注册、登录等认证相关接口")
public class AuthController {

    private final UserService userService;

    /**
     * 用户注册
     * POST /api/auth/register
     */
    @Operation(
            summary = "用户注册",
            description = "注册新用户，注册成功后自动登录并返回 Token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "注册成功",
                    content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "400", description = "请求参数错误（用户名已存在、邮箱已被注册等）"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @PostMapping("/register")
    public Result<LoginResultDTO> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "注册信息", 
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegisterDTO.class))
            )
            @Valid @RequestBody RegisterDTO registerDTO) {
        try {
            // 1. 注册用户
            userService.register(registerDTO);

            // 2. 注册成功后自动登录，返回 Token
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setUsernameOrEmail(registerDTO.getUsername());
            loginDTO.setPassword(registerDTO.getPassword());
            LoginResultDTO loginResult = userService.login(loginDTO);

            return Result.success(loginResult);
        } catch (RuntimeException e) {
            log.error("用户注册失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("用户注册异常", e);
            return Result.error("注册失败，请稍后重试");
        }
    }

    /**
     * 用户登录
     * POST /api/auth/login
     */
    @Operation(
            summary = "用户登录",
            description = "使用用户名或邮箱登录，返回 JWT Token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登录成功",
                    content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "401", description = "用户名/密码错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @PostMapping("/login")
    public Result<LoginResultDTO> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "登录信息",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginDTO.class))
            )
            @Valid @RequestBody LoginDTO loginDTO) {
        try {
            LoginResultDTO loginResult = userService.login(loginDTO);
            return Result.success(loginResult);
        } catch (RuntimeException e) {
            log.error("用户登录失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("用户登录异常", e);
            return Result.error("登录失败，请稍后重试");
        }
    }
}

