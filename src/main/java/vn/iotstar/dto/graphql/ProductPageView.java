package vn.iotstar.dto.graphql;

import java.util.List;

public record ProductPageView(List<ProductView> items, int page, int size, int totalItems, int totalPages) {
}
