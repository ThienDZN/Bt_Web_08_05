package vn.iotstar.controller.admin;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.entity.Category;
import vn.iotstar.service.ICategoryService;
import vn.iotstar.service.impl.CategoryServiceImpl;
import vn.iotstar.util.LocalImageStorage;
import vn.iotstar.validation.ValidationErrors;
import vn.iotstar.validation.ValidationUtils;

@Controller
public class CategoryController {
    private static final String LIST_TEMPLATE = "admin/category-list";
    private static final String ADD_TEMPLATE = "admin/category-add";
    private static final String EDIT_TEMPLATE = "admin/category-edit";

    private final ICategoryService categoryService;

    public CategoryController() {
        this(new CategoryServiceImpl());
    }

    CategoryController(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/admin/categories")
    public String showList(@RequestParam(required = false) String keyword,
                           @RequestParam(required = false) String message,
                           Model model) {
        ValidationErrors errors = new ValidationErrors();
        String normalizedKeyword = ValidationUtils.trimToNull(keyword);
        if (ValidationUtils.exceedsLength(normalizedKeyword, 50)) {
            errors.add("keyword", "Search keyword must not exceed 50 characters.");
        }

        List<Category> categories = errors.hasErrors()
                ? categoryService.findAll()
                : (normalizedKeyword == null ? categoryService.findAll()
                : categoryService.searchByName(normalizedKeyword));

        model.addAttribute("listcate", categories);
        model.addAttribute("keyword", ValidationUtils.emptyIfNull(normalizedKeyword));
        model.addAttribute("errors", errors.asMap());
        if (message != null && !message.isBlank()) {
            model.addAttribute("message", message);
        }
        return LIST_TEMPLATE;
    }

    @GetMapping("/admin/category/add")
    public String showAddForm(Model model) {
        bindFormState(model, new Category(), new ValidationErrors());
        return ADD_TEMPLATE;
    }

    @GetMapping("/admin/category/edit")
    public String showEditForm(@RequestParam(required = false) String id,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        Integer categoryId = parseId(id);
        if (categoryId == null) {
            return redirectWithMessage(redirectAttributes, "The provided category id is not valid.");
        }

        Category category = categoryService.findById(categoryId);
        if (category == null) {
            return redirectWithMessage(redirectAttributes, "Category entry not found.");
        }

        bindFormState(model, category, new ValidationErrors());
        return EDIT_TEMPLATE;
    }

    @PostMapping("/admin/category/insert")
    public String insertCategory(@RequestParam(required = false) String categoryname,
                                 @RequestParam(required = false) String images,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(name = "images1", required = false) MultipartFile imageFile,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        Category category = new Category();
        ValidationErrors errors = validateCategoryRequest(category, categoryname, images, status, null, null);
        if (errors.hasErrors()) {
            bindFormState(model, category, errors);
            return ADD_TEMPLATE;
        }

        try {
            category.setImages(resolveImage(imageFile, null));
        } catch (IllegalArgumentException | IOException e) {
            errors.add("images1", e.getMessage());
            bindFormState(model, category, errors);
            return ADD_TEMPLATE;
        }

        try {
            categoryService.insert(category);
            return redirectWithMessage(redirectAttributes, "Category created successfully.");
        } catch (RuntimeException e) {
            if (LocalImageStorage.hasUpload(imageFile)) {
                deleteLocalImageQuietly(category.getImages());
            }
            model.addAttribute("error", "Could not create the category. Please review the entered data.");
            bindFormState(model, category, errors);
            return ADD_TEMPLATE;
        }
    }

    @PostMapping("/admin/category/update")
    public String updateCategory(@RequestParam(required = false) String categoryid,
                                 @RequestParam(required = false) String categoryname,
                                 @RequestParam(required = false) String images,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(name = "images1", required = false) MultipartFile imageFile,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        Integer categoryId = parseId(categoryid);
        if (categoryId == null) {
            return redirectWithMessage(redirectAttributes, "The provided category id is not valid.");
        }

        Category category = categoryService.findById(categoryId);
        if (category == null) {
            return redirectWithMessage(redirectAttributes, "Category does not exist.");
        }

        String previousImage = category.getImages();
        ValidationErrors errors = validateCategoryRequest(
                category, categoryname, images, status, categoryId, previousImage);
        if (errors.hasErrors()) {
            bindFormState(model, category, errors);
            return EDIT_TEMPLATE;
        }

        boolean hasNewImage = LocalImageStorage.hasUpload(imageFile);
        try {
            category.setImages(resolveImage(imageFile, previousImage));
        } catch (IllegalArgumentException | IOException e) {
            errors.add("images1", e.getMessage());
            bindFormState(model, category, errors);
            return EDIT_TEMPLATE;
        }

        try {
            categoryService.update(category);
            if (hasNewImage && isLocalImage(previousImage)) {
                try {
                    LocalImageStorage.deleteIfExists(previousImage);
                } catch (IOException e) {
                    return redirectWithMessage(redirectAttributes,
                            "Category updated, but its old image could not be removed.");
                }
            }
            return redirectWithMessage(redirectAttributes, "Category updated successfully.");
        } catch (RuntimeException e) {
            if (hasNewImage) {
                deleteLocalImageQuietly(category.getImages());
            }
            model.addAttribute("error", "Could not update the category. Please review the entered data.");
            bindFormState(model, category, errors);
            return EDIT_TEMPLATE;
        }
    }

    @PostMapping("/admin/category/delete")
    public String deleteCategory(@RequestParam(required = false) String id,
                                 RedirectAttributes redirectAttributes) {
        Integer categoryId = parseId(id);
        if (categoryId == null) {
            return redirectWithMessage(redirectAttributes, "The provided category id is not valid.");
        }

        Category category = categoryService.findById(categoryId);
        if (category == null) {
            return redirectWithMessage(redirectAttributes, "Category does not exist.");
        }

        try {
            categoryService.delete(categoryId);
        } catch (Exception e) {
            return redirectWithMessage(redirectAttributes,
                    "Could not delete this category because it may still be used by a product.");
        }

        if (isLocalImage(category.getImages())) {
            try {
                LocalImageStorage.deleteIfExists(category.getImages());
            } catch (IOException e) {
                return redirectWithMessage(redirectAttributes,
                        "Category was deleted, but its old image could not be removed.");
            }
        }
        return redirectWithMessage(redirectAttributes, "Category deleted successfully.");
    }

    private ValidationErrors validateCategoryRequest(Category category, String categoryNameValue,
                                                      String imageValue, String statusValue,
                                                      Integer currentId, String previousImage) {
        ValidationErrors errors = new ValidationErrors();

        String categoryName = ValidationUtils.trimToNull(categoryNameValue);
        if (categoryName == null) {
            category.setCategoryname("");
            errors.add("categoryname", "Please enter the category name.");
        } else if (ValidationUtils.exceedsLength(categoryName, 50)) {
            category.setCategoryname(categoryName);
            errors.add("categoryname", "Category name must not exceed 50 characters.");
        } else {
            category.setCategoryname(categoryName);
            Category duplicate = categoryService.findByCategoryname(categoryName);
            if (duplicate != null && (currentId == null || duplicate.getCategoryid() != currentId)) {
                errors.add("categoryname", "The category name already exists.");
            }
        }

        String imageReference = ValidationUtils.trimToNull(imageValue);
        if (ValidationUtils.exceedsLength(imageReference, 500)) {
            errors.add("images", "Image URL must not exceed 500 characters.");
        } else if (imageReference != null && !isAcceptedImageReference(imageReference, previousImage)) {
            errors.add("images", "Image URL must start with http:// or https://.");
        }
        category.setImages(imageReference == null ? ValidationUtils.trimToNull(previousImage) : imageReference);

        String normalizedStatus = ValidationUtils.trimToNull(statusValue);
        if (!ValidationUtils.isStatusValue(normalizedStatus)) {
            errors.add("status", "Please select a valid status.");
        } else {
            category.setStatus("1".equals(normalizedStatus) ? 1 : 0);
        }
        return errors;
    }

    private void bindFormState(Model model, Category category, ValidationErrors errors) {
        model.addAttribute("cate", category);
        model.addAttribute("errors", errors.asMap());
    }

    private String resolveImage(MultipartFile imageFile, String previousImage) throws IOException {
        if (LocalImageStorage.hasUpload(imageFile)) {
            return LocalImageStorage.storeImage(imageFile, "category");
        }
        return ValidationUtils.trimToNull(previousImage);
    }

    private void deleteLocalImageQuietly(String imageName) {
        if (!isLocalImage(imageName)) {
            return;
        }
        try {
            LocalImageStorage.deleteIfExists(imageName);
        } catch (IOException ignored) {
            // A failed cleanup must not replace the validation error shown to the user.
        }
    }

    private String redirectWithMessage(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/admin/categories";
    }

    private Integer parseId(String rawId) {
        try {
            int id = Integer.parseInt(rawId);
            return id > 0 ? id : null;
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    private boolean isAcceptedImageReference(String value, String previousImage) {
        return ValidationUtils.isValidHttpUrl(value)
                || (isLocalImage(previousImage) && value.equals(ValidationUtils.trimToNull(previousImage)));
    }

    private boolean isLocalImage(String value) {
        return LocalImageStorage.isLocalFile(value);
    }
}
