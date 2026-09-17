package vn.iotstar.controller.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import vn.iotstar.entity.Category;

class CategoryControllerTest {

    @Test
    void listRendersThymeleafTemplateAndUsesSearchTerm() {
        CategoryServiceStub categoryService = new CategoryServiceStub();
        Category category = new Category();
        category.setCategoryname("Playlist Opener");
        categoryService.searchResults = List.of(category);
        CategoryController controller = new CategoryController(categoryService);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.showList(" playlist ", null, model);

        assertEquals("admin/category-list", view);
        assertEquals("playlist", model.getAttribute("keyword"));
        assertEquals(List.of(category), model.getAttribute("listcate"));
        assertEquals("playlist", categoryService.searchedKeyword);
    }

    @Test
    @SuppressWarnings("unchecked")
    void createRejectsBlankCategoryNameBeforePersistence() {
        CategoryServiceStub categoryService = new CategoryServiceStub();
        CategoryController controller = new CategoryController(categoryService);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.insertCategory(" ", null, "1", null, model,
                new RedirectAttributesModelMap());

        assertEquals("admin/category-add", view);
        Map<String, String> errors = (Map<String, String>) model.getAttribute("errors");
        assertTrue(errors.containsKey("categoryname"));
    }

    @Test
    void malformedEditIdRedirectsToTheCategoryList() {
        CategoryServiceStub categoryService = new CategoryServiceStub();
        CategoryController controller = new CategoryController(categoryService);
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = controller.showEditForm("not-a-number", redirectAttributes, new ExtendedModelMap());

        assertEquals("redirect:/admin/categories", view);
        assertTrue(redirectAttributes.getFlashAttributes().containsKey("message"));
    }

    @Test
    void categoryTemplatesUseLayoutDialect() {
        assertTemplateContains("templates/layout/base.html", "layout:fragment=\"content\"");
        assertTemplateContains("templates/admin/category-list.html", "layout:decorate=\"~{layout/base}\"");
        assertTemplateContains("templates/admin/category-add.html", "th:object=\"${cate}\"");
        assertTemplateContains("templates/admin/category-edit.html", "th:field=\"*{categoryid}\"");
    }

    private void assertTemplateContains(String resource, String expectedText) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertTrue(input != null, () -> "Missing template: " + resource);
            String content = new String(input.readAllBytes());
            assertFalse(content.isBlank());
            assertTrue(content.contains(expectedText), () -> resource + " does not contain " + expectedText);
        } catch (Exception e) {
            throw new AssertionError("Could not read " + resource, e);
        }
    }
}
