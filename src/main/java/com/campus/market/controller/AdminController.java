package com.campus.market.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.annotation.OperationLog;
import com.campus.market.annotation.RequireAdmin;
import com.campus.market.common.BusinessException;
import com.campus.market.common.PageResult;
import com.campus.market.common.ProductStatus;
import com.campus.market.common.Result;
import com.campus.market.entity.Product;
import com.campus.market.entity.User;
import com.campus.market.mapper.ProductMapper;
import com.campus.market.mapper.UserMapper;
import com.campus.market.vo.ProductCardVO;
import com.campus.market.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 管理端：全部接口 @RequireAdmin，普通用户访问返回 403 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserMapper userMapper;
    private final ProductMapper productMapper;

    @RequireAdmin
    @GetMapping("/users")
    public Result<PageResult<UserVO>> users(@RequestParam(defaultValue = "1") int pageNo,
                                            @RequestParam(defaultValue = "10") int size) {
        Page<User> page = new Page<>(pageNo, Math.min(size, 50));
        return Result.success(PageResult.of(userMapper.selectPage(page, null), UserVO::of));
    }

    @RequireAdmin
    @GetMapping("/products/pending")
    public Result<PageResult<Product>> pendingProducts(@RequestParam(defaultValue = "1") int pageNo,
                                                       @RequestParam(defaultValue = "10") int size) {
        Page<Product> page = new Page<>(pageNo, Math.min(size, 50));
        return Result.success(PageResult.of(productMapper.selectPage(page,
                Wrappers.<Product>lambdaQuery().eq(Product::getStatus, ProductStatus.PENDING)
                        .orderByAsc(Product::getCreateTime))));
    }

    /** 审核：通过 → 在售；驳回 → 已下架 */
    @OperationLog(module = "管理端", action = "商品审核")
    @RequireAdmin
    @PutMapping("/product/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @RequestParam boolean pass) {
        Product p = productMapper.selectById(id);
        if (p == null) throw new BusinessException(404, "商品不存在");
        if (p.getStatus() != ProductStatus.PENDING) throw new BusinessException("该商品不在待审核状态");

        productMapper.update(null, Wrappers.<Product>lambdaUpdate()
                .set(Product::getStatus, pass ? ProductStatus.ON_SALE : ProductStatus.OFF_SHELF)
                .eq(Product::getId, id));
        return Result.success();
    }
}
