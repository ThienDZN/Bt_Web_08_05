package vn.iotstar.service.impl;

import java.util.List;

import vn.iotstar.dao.ICategoryDao;
import vn.iotstar.dao.impl.CategoryDao;
import vn.iotstar.entity.Category;
import vn.iotstar.service.ICategoryService;

public class CategoryServiceImpl implements ICategoryService {
    private final ICategoryDao categoryDao = new CategoryDao();

    @Override
    public void insert(Category category) {
        validateCategory(category);
        Category existing = findByCategoryname(category.getCategoryname());
        if (existing != null) {
            throw new IllegalArgumentException("The category name already exists.");
        }
        categoryDao.insert(category);
    }

    @Override
    public int count() {
        return categoryDao.count();
    }

    @Override
    public int count(String keyword) {
        return categoryDao.count(keyword);
    }

    @Override
    public List<Category> findAll(int page, int pagesize) {
        return categoryDao.findAll(page, pagesize);
    }

    @Override
    public List<Category> findPage(String keyword, int page, int pageSize) {
        return categoryDao.findPage(keyword, page, pageSize);
    }

    @Override
    public List<Category> searchByName(String catname) {
        return categoryDao.searchByName(catname);
    }

    @Override
    public List<Category> findAll() {
        return categoryDao.findAll();
    }

    @Override
    public Category findById(int cateid) {
        return categoryDao.findById(cateid);
    }

    @Override
    public void delete(int cateid) throws Exception {
        categoryDao.delete(cateid);
    }

    @Override
    public void update(Category category) {
        validateCategory(category);
        Category current = findById(category.getCategoryid());
        if (current == null) {
            throw new IllegalArgumentException("The category does not exist.");
        }

        Category duplicate = findByCategoryname(category.getCategoryname());
        if (duplicate != null && duplicate.getCategoryid() != category.getCategoryid()) {
            throw new IllegalArgumentException("The category name already exists.");
        }

        categoryDao.update(category);
    }

    @Override
    public Category findByCategoryname(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return categoryDao.findByCategoryname(name.trim());
    }

    private void validateCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("The category data is not valid.");
        }
        if (category.getCategoryname() == null || category.getCategoryname().trim().isEmpty()) {
            throw new IllegalArgumentException("The category name must not be empty.");
        }
        if (category.getStatus() != 0 && category.getStatus() != 1) {
            throw new IllegalArgumentException("The category status must be either 0 or 1.");
        }
        category.setCategoryname(category.getCategoryname().trim());
    }
}
