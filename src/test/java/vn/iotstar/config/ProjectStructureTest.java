package vn.iotstar.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ProjectStructureTest {
    @Test
    void uploadDirectoryConstantShouldExist() {
        assertNotNull(UploadConstants.DIR);
    }

    @Test
    void productPageSizeShouldLoad() {
        assertNotNull(AppProperties.get("app.product.page-size", "6"));
    }
}
