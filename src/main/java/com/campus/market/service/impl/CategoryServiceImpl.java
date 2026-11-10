package com.campus.market.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.market.entity.Category;
import com.campus.market.mapper.CategoryMapper;
import com.campus.market.service.CategoryService;
import com.campus.market.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 分类树：一次全量查询 + 内存递归构树，避免 N+1 查询 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryVO> tree() {
        List<Category> all = categoryMapper.selectList(
                Wrappers.<Category>lambdaQuery().orderByAsc(Category::getSort));

        List<CategoryVO> nodes = all.stream().map(c -> {
            CategoryVO vo = new CategoryVO();
            vo.setId(c.getId());
            vo.setParentId(c.getParentId());
            vo.setName(c.getName());
            vo.setSort(c.getSort());
            return vo;
        }).toList();

        Map<Long, List<CategoryVO>> byParent = nodes.stream()
                .collect(Collectors.groupingBy(CategoryVO::getParentId));

        List<CategoryVO> roots = new ArrayList<>();
        for (CategoryVO node : nodes) {
            List<CategoryVO> children = byParent.getOrDefault(node.getId(), List.of());
            node.setChildren(new ArrayList<>(children));
            if (node.getParentId() == null || node.getParentId() == 0L) {
                roots.add(node);
            }
        }
        return roots;
    }

    @Override
    public void clearCache() {
        // TODO 第 7.3 节：分类变更后删掉 category:tree 缓存
        // redisUtil.delete("category:tree");
    }
}
