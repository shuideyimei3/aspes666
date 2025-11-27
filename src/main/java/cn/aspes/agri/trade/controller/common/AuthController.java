package cn.aspes.agri.trade.controller.common;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.dto.LoginRequest;
import cn.aspes.agri.trade.dto.LoginResponse;
import cn.aspes.agri.trade.dto.UserRegisterRequest;
import cn.aspes.agri.trade.dto.UserUpdateRequest;
import cn.aspes.agri.trade.security.CustomUserDetails;
import cn.aspes.agri.trade.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 通用 - 认证（登录、注册）控制器
 */
@Tag(name = "通用 - 认证与授权")
@RestController
@RequestMapping("/api/common/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }
    
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Long> register(@Valid @RequestBody UserRegisterRequest request) {
        Long userId = userService.register(request);
        return Result.success(userId);
    }
    
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        userService.logout(token);
        return Result.success();
    }
    
    @Operation(summary = "修改用户个人信息")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @Valid @RequestBody UserUpdateRequest request) {
        userService.updateUserInfo(userDetails.getId(), request);
        return Result.success();
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}