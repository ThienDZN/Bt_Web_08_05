package vn.iotstar.controller.admin;

import java.util.ArrayList;
import java.util.List;

import vn.iotstar.entity.Category;
import vn.iotstar.service.ICategoryService;

final class CategoryServiceStub implements ICategoryService {
    List<Category> allCategories = new ArrayList<>();
    List<Category> searchResults = new ArrayList<>();
    Category categoryById;
    Category duplicate;
    String searchedKeyword;

    @Override
    public void insert(Category category) {
        allCategories.add(category);
    }

    @Override
    public int count() {
        return allCategories.size();
    }

    @Override
    public int count(String keyword) {
        return searchResults.size();
    }

    @Override
    public List<Category> findAll(int page, int pageSize) {
        return allCategories;
    }

    @Override
    public List<Category> findPage(String keyword, int page, int pageSize) {
        return keyword == null || keyword.isBlank() ? allCategories : searchResults;
    }

    @Override
    public List<Category> searchByName(String categoryName) {
        searchedKeyword = categoryName;
        return searchResults;
    }

    @Override
    public List<Category> findAll() {
        return allCategories;
    }

    @Override
    public Category findById(int categoryId) {
        return categoryById;
    }

    @Override
    public void delete(int categoryId) {
        allCategories.removeIf(category -> category.getCategoryid() == categoryId);
    }

    @Override
    public void update(Category category) {
        categoryById = category;
    }

    @Override
    public Category findByCategoryname(String name) {
        return duplicate;
    }
}
