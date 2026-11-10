package com.campus.market.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductCreateDTO {

    @NotNull(message = "请选择分类")
    private Long categoryId;

    @NotBlank(message = "标题不能为空")
    @Size(max = 64, message = "标题最长 64 字")
    private String title;

    @Size(max = 2000, message = "描述最长 2000 字")
    private String description;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于 0")
    private BigDecimal price;

    private BigDecimal originalPrice;

    /** 封面图地址（由 /api/file/upload 返回） */
    private String cover;

    /** 逗号分隔的图片地址 */
    private String images;

    private Integer stock = 1;
}
