package com.campus.market.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.market.dto.OrderCreateDTO;
import com.campus.market.vo.OrderVO;

public interface OrderService {

    Long create(OrderCreateDTO dto, Long buyerId);

    void pay(String orderNo, Long buyerId);

    void cancel(String orderNo, Long userId);

    void finish(String orderNo, Long buyerId);

    IPage<OrderVO> pageMine(int pageNo, int size, Integer status, Long userId);
}
