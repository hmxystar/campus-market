package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 分类表：只有 create_time，没有 update_time / deleted，所以不继承 BaseEntity */
@Data
@TableName("category")
public class Category implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 0 表示一级分类 */
    private Long parentId;
    private String name;
    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
