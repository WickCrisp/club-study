package com.crisp.demo.domain.club.api;

import com.crisp.demo.domain.club.infra.ClubCategoryMapper;
import com.crisp.demo.domain.club.model.ClubCategory;
import com.crisp.demo.shared.error.BusinessException;
import com.crisp.demo.shared.result.Result;
import com.crisp.demo.shared.security.RequireRole;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 社团分类 — 浏览公开，维护仅管理员
 */
@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final ClubCategoryMapper categoryMapper;

    /** 分类列表（公开） */
    @GetMapping("/api/club-category/list")
    public Result<List<ClubCategory>> list() {
        return Result.success(categoryMapper.findActive());
    }

    /** 分类列表（管理员，含停用） */
    @GetMapping("/api/club-category/all")
    @RequireRole(value = "ADMIN", action = "查看全部分类")
    public Result<List<ClubCategory>> all() {
        return Result.success(categoryMapper.findAll());
    }

    /** 新增分类 */
    @PostMapping("/api/club-category")
    @RequireRole(value = "ADMIN", action = "新增分类")
    public Result<ClubCategory> create(@RequestBody ClubCategory category) {
        String name = category.getName() == null ? "" : category.getName().trim();
        if (name.isEmpty()) {
            throw new BusinessException("分类名称不能为空");
        }
        category.setName(name);
        if (category.getSort() == null) category.setSort(0);
        if (category.getStatus() == null || category.getStatus().isEmpty()) category.setStatus("ACTIVE");
        try {
            categoryMapper.insert(category);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("分类名称已存在：" + name);
        }
        return Result.success("创建成功", categoryMapper.findById(category.getId()));
    }

    /** 修改分类 */
    @PutMapping("/api/club-category/{id}")
    @RequireRole(value = "ADMIN", action = "修改分类")
    public Result<ClubCategory> update(@PathVariable Long id, @RequestBody ClubCategory category) {
        ClubCategory exist = categoryMapper.findById(id);
        if (exist == null) {
            throw new BusinessException("分类不存在");
        }
        category.setId(id);
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            category.setName(exist.getName());
        }
        if (category.getSort() == null) category.setSort(exist.getSort());
        if (category.getStatus() == null || category.getStatus().isEmpty()) category.setStatus(exist.getStatus());
        try {
            categoryMapper.update(category);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("分类名称已存在：" + category.getName());
        }
        return Result.success("更新成功", categoryMapper.findById(id));
    }

    /** 删除分类（社团的 category_id 会被置空） */
    @DeleteMapping("/api/club-category/{id}")
    @RequireRole(value = "ADMIN", action = "删除分类")
    public Result<?> delete(@PathVariable Long id) {
        if (categoryMapper.findById(id) == null) {
            throw new BusinessException("分类不存在");
        }
        categoryMapper.deleteById(id);
        return Result.success("删除成功");
    }
}
