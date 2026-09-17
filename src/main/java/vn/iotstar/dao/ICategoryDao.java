package vn.iotstar.dao;

import java.util.List;

import vn.iotstar.entity.Category;

public interface ICategoryDao {
    void insert(Category category);

    int count();

    int count(String keyword);

    List<Category> findAll(int page, int pagesize);

    List<Category> findPage(String keyword, int page, int pageSize);

    List<Category> searchByName(String catname);

    List<Category> findAll();

    Category findById(int cateid);

    void delete(int cateid) throws Exception;

    void update(Category category);

    Category findByCategoryname(String name);
}
