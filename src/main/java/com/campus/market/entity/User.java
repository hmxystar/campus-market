package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    private String username;
    /** BCrypt 哈希，禁止存明文 */
    private String password;
    private String nickname;
    private String avatar;
    private String phone;
    /** 0-用户 1-管理员 */
    private Integer role;
    /** 1-正常 0-封禁 */
    private Integer status;
}
