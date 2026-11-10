package com.campus.market.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCreateDTO {

    @NotNull(message = "商品 id 不能为空")
    private Long productId;
}
