package vn.iotstar.dao;

import java.util.List;

import vn.iotstar.entity.Product;

public interface IProductDao {
    void insert(Product product);
    void update(Product product);
    void delete(Long productId) throws Exception;
    Product findById(Long productId);
    List<Product> findAll();
    List<Product> findLatestActive(int limit);
    List<Product> findByPriceAscending();
    List<Product> findByCategoryId(int categoryId);
    List<Product> findActive(int page, int pageSize);
    int countActive();
    List<Product> findPage(String keyword, int page, int pageSize);
    int count(String keyword);
}
