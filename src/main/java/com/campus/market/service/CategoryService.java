package com.campus.market.service;

import com.campus.market.vo.CategoryVO;

import java.util.List;

public interface CategoryService {

    /** 一次查询 + 内存递归构树 */
    List<CategoryVO> tree();

    /** 分类增删改后清理缓存（第 7.3 节） */
    void clearCache();
}
