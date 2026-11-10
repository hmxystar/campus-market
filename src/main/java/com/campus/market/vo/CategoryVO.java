package com.campus.market.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 分类树节点：children 为空时前端按一级分类渲染 */
@Data
public class CategoryVO {

    private Long id;
    private Long parentId;
    private String name;
    private Integer sort;
    private List<CategoryVO> children = new ArrayList<>();
}
