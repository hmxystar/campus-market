package com.campus.market.controller;

import com.campus.market.annotation.OperationLog;
import com.campus.market.annotation.RequireLogin;
import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.common.UserContext;
import com.campus.market.dto.OrderCreateDTO;
import com.campus.market.service.OrderService;
import com.campus.market.vo.OrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@RequireLogin
public class OrderController {

    private final OrderService orderService;

    @OperationLog(module = "订单", action = "下单")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody OrderCreateDTO dto) {
        return Result.success(orderService.create(dto, UserContext.getUserId()));
    }

    @OperationLog(module = "订单", action = "支付")
    @PostMapping("/{orderNo}/pay")
    public Result<Void> pay(@PathVariable String orderNo) {
        orderService.pay(orderNo, UserContext.getUserId());
        return Result.success();
    }

    @OperationLog(module = "订单", action = "取消")
    @PostMapping("/{orderNo}/cancel")
    public Result<Void> cancel(@PathVariable String orderNo) {
        orderService.cancel(orderNo, UserContext.getUserId());
        return Result.success();
    }

    @OperationLog(module = "订单", action = "完成")
    @PostMapping("/{orderNo}/finish")
    public Result<Void> finish(@PathVariable String orderNo) {
        orderService.finish(orderNo, UserContext.getUserId());
        return Result.success();
    }

    @GetMapping("/mine")
    public Result<PageResult<OrderVO>> mine(@RequestParam(defaultValue = "1") int pageNo,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) Integer status) {
        return Result.success(PageResult.of(
                orderService.pageMine(pageNo, size, status, UserContext.getUserId())));
    }
}
