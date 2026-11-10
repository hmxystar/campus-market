package com.campus.market.controller;

import com.campus.market.annotation.OperationLog;
import com.campus.market.annotation.RequireLogin;
import com.campus.market.common.Result;
import com.campus.market.common.UserContext;
import com.campus.market.dto.LoginDTO;
import com.campus.market.dto.RegisterDTO;
import com.campus.market.service.UserService;
import com.campus.market.vo.LoginVO;
import com.campus.market.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    @OperationLog(module = "用户", action = "登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @RequireLogin
    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.success(userService.info(UserContext.getUserId()));
    }
}
