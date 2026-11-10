package com.campus.market.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

/**
 * 分页返回体：把 MyBatis-Plus 的 IPage 转成前端好用的结构。
 */
@Data
public class PageResult<T> implements Serializable {

    private List<T> records;
    private long total;
    private long pageNo;
    private long pageSize;
    private long pages;

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> r = new PageResult<>();
        r.records = page.getRecords();
        r.total = page.getTotal();
        r.pageNo = page.getCurrent();
        r.pageSize = page.getSize();
        r.pages = page.getPages();
        return r;
    }

    /** 需要把实体转成 VO 时，用这个重载，避免多查一次 */
    public static <E, T> PageResult<T> of(IPage<E> page, Function<E, T> mapper) {
        PageResult<T> r = new PageResult<>();
        r.records = page.getRecords().stream().map(mapper).toList();
        r.total = page.getTotal();
        r.pageNo = page.getCurrent();
        r.pageSize = page.getSize();
        r.pages = page.getPages();
        return r;
    }
}
