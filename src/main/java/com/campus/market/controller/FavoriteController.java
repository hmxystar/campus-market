package com.campus.market.controller;

import com.campus.market.annotation.RequireLogin;
import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.common.UserContext;
import com.campus.market.service.FavoriteService;
import com.campus.market.vo.ProductCardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
@RequireLogin
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{productId}")
    public Result<Void> add(@PathVariable Long productId) {
        favoriteService.add(productId, UserContext.getUserId());
        return Result.success();
    }

    @DeleteMapping("/{productId}")
    public Result<Void> cancel(@PathVariable Long productId) {
        favoriteService.cancel(productId, UserContext.getUserId());
        return Result.success();
    }

    @GetMapping("/mine")
    public Result<PageResult<ProductCardVO>> mine(@RequestParam(defaultValue = "1") int pageNo,
                                                  @RequestParam(defaultValue = "10") int size) {
        return Result.success(PageResult.of(
                favoriteService.pageMine(pageNo, size, UserContext.getUserId())));
    }
}
