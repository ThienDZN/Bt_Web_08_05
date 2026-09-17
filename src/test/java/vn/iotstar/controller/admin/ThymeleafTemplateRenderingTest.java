package vn.iotstar.controller.admin;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.List;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import vn.iotstar.entity.Category;

class ThymeleafTemplateRenderingTest {

    @Test
    void categoryViewsRenderWithTheSharedLayoutAndFormBinding() throws Exception {
        CategoryServiceStub categoryService = new CategoryServiceStub();
        Category category = new Category();
        category.setCategoryid(7);
        category.setCategoryname("Playlist Opener");
        category.setImages("https://example.com/playlist.jpg");
        category.setStatus(1);
        categoryService.allCategories = List.of(category);
        categoryService.categoryById = category;

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CategoryController(categoryService))
                .setViewResolvers(createViewResolver())
                .build();

        mockMvc.perform(get("/admin/categories"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Thymeleaf Layout Dialect")))
                .andExpect(content().string(containsString("Playlist Opener")));
        mockMvc.perform(get("/admin/category/add"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Create Category")))
                .andExpect(content().string(containsString("Sinh viên thực hiện:")));
        mockMvc.perform(get("/admin/category/edit").param("id", "7"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Update Category")))
                .andExpect(content().string(containsString("value=\"Playlist Opener\"")));
    }

    private ThymeleafViewResolver createViewResolver() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());

        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        templateEngine.addDialect(new LayoutDialect());

        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(templateEngine);
        viewResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        return viewResolver;
    }
}
