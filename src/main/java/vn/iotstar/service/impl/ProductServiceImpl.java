package vn.iotstar.service.impl;

import java.math.BigDecimal;
import java.util.List;

import vn.iotstar.dao.IProductDao;
import vn.iotstar.dao.impl.ProductDao;
import vn.iotstar.entity.Product;
import vn.iotstar.service.IProductService;

public class ProductServiceImpl implements IProductService {
    private final IProductDao productDao = new ProductDao();

    @Override
    public void insert(Product product) {
        validate(product);
        productDao.insert(product);
    }

    @Override
    public void update(Product product) {
        validate(product);
        productDao.update(product);
    }

    @Override
    public void delete(Long productId) throws Exception {
        productDao.delete(productId);
    }

    @Override
    public Product findById(Long productId) {
        return productDao.findById(productId);
    }

    @Override
    public List<Product> findAll() {
        return productDao.findAll();
    }

    @Override
    public List<Product> findLatestActive(int limit) {
        return productDao.findLatestActive(limit);
    }

    @Override
    public List<Product> findByPriceAscending() {
        return productDao.findByPriceAscending();
    }

    @Override
    public List<Product> findByCategoryId(int categoryId) {
        return productDao.findByCategoryId(categoryId);
    }

    @Override
    public List<Product> findActive(int page, int pageSize) {
        return productDao.findActive(page, pageSize);
    }

    @Override
    public int countActive() {
        return productDao.countActive();
    }

    @Override
    public List<Product> findPage(String keyword, int page, int pageSize) {
        return productDao.findPage(keyword, page, pageSize);
    }

    @Override
    public int count(String keyword) {
        return productDao.count(keyword);
    }

    private void validate(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product data is not valid.");
        }
        if (product.getProductName() == null || product.getProductName().isBlank()) {
            throw new IllegalArgumentException("The title field must not be empty.");
        }
        if (product.getCategory() == null) {
            throw new IllegalArgumentException("Please choose a category.");
        }
        BigDecimal price = product.getPrice();
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("The price value is not valid.");
        }
        if (product.getQuantity() < 0) {
            throw new IllegalArgumentException("The quantity value is not valid.");
        }
        product.setProductName(product.getProductName().trim());
        if (product.getDescription() != null) {
            product.setDescription(product.getDescription().trim());
        }
    }
}
