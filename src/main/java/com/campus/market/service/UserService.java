package com.campus.market.service;

import com.campus.market.dto.LoginDTO;
import com.campus.market.dto.RegisterDTO;
import com.campus.market.vo.LoginVO;
import com.campus.market.vo.UserVO;

public interface UserService {

    void register(RegisterDTO dto);

    LoginVO login(LoginDTO dto);

    /** 当前登录用户信息 */
    UserVO info(Long userId);
}
