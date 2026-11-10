package com.campus.market.controller;

import com.campus.market.annotation.OperationLog;
import com.campus.market.annotation.RateLimit;
import com.campus.market.annotation.RequireLogin;
import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.common.UserContext;
import com.campus.market.dto.ProductCreateDTO;
import com.campus.market.service.FileService;
import com.campus.market.service.ProductService;
import com.campus.market.vo.ProductCardVO;
import com.campus.market.vo.ProductDetailVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final FileService fileService;

    @RequireLogin
    @RateLimit(limit = 10, seconds = 60)
    @PostMapping("/file/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        return Result.success(Map.of("url", fileService.save(file)));
    }

    @OperationLog(module = "商品", action = "发布")
    @RateLimit(limit = 5, seconds = 60)
    @RequireLogin
    @PostMapping("/product")
    public Result<Long> publish(@Valid @RequestBody ProductCreateDTO dto) {
        return Result.success(productService.publish(dto, UserContext.getUserId()));
    }

    /** 公开接口：不用登录也能逛列表 */
    @GetMapping("/product")
    public Result<PageResult<ProductCardVO>> page(@RequestParam(defaultValue = "1") int pageNo,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(required = false) Long categoryId,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) String sort) {
        return Result.success(PageResult.of(productService.page(pageNo, size, categoryId, keyword, sort)));
    }

    @GetMapping("/product/{id}")
    public Result<ProductDetailVO> detail(@PathVariable Long id) {
        return Result.success(productService.detail(id));
    }

    @OperationLog(module = "商品", action = "编辑")
    @RequireLogin
    @PutMapping("/product/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProductCreateDTO dto) {
        productService.update(id, dto, UserContext.getUserId());
        return Result.success();
    }

    @OperationLog(module = "商品", action = "下架")
    @RequireLogin
    @DeleteMapping("/product/{id}")
    public Result<Void> offShelf(@PathVariable Long id) {
        productService.offShelf(id, UserContext.getUserId());
        return Result.success();
    }
}
