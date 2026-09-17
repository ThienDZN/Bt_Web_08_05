package vn.iotstar.dto.graphql;

import java.math.BigDecimal;

public record ProductView(Long productId, String productName, String description, BigDecimal price,
                          int quantity, String image, int status, CategoryView category) {
}
