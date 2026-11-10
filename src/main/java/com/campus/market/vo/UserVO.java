package com.campus.market.vo;

import com.campus.market.entity.User;
import lombok.Data;

@Data
public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String phone;
    private Integer role;
    private Integer status;

    /** 不返回 password，避免越权泄露 */
    public static UserVO of(User u) {
        UserVO vo = new UserVO();
        vo.setId(u.getId());
        vo.setUsername(u.getUsername());
        vo.setNickname(u.getNickname());
        vo.setAvatar(u.getAvatar());
        vo.setPhone(u.getPhone());
        vo.setRole(u.getRole());
        vo.setStatus(u.getStatus());
        return vo;
    }
}
