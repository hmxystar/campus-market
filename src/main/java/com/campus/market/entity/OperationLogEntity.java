package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 操作日志表：不参与逻辑删除，字段也不同，故单独定义 */
@Data
@TableName("operation_log")
public class OperationLogEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String module;
    private String action;
    private String method;
    private String params;
    private Long costMs;
    /** 1-成功 0-失败 */
    private Integer success;
    private String ip;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
