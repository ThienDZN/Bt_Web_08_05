package vn.iotstar.dto.graphql;

import java.util.List;

public record CategoryPageView(List<CategoryView> items, int page, int size, int totalItems, int totalPages) {
}
