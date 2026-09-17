package vn.iotstar.controller;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.iotstar.config.AppProperties;
import vn.iotstar.entity.Product;
import vn.iotstar.service.IProductService;
import vn.iotstar.service.impl.ProductServiceImpl;
import vn.iotstar.util.SongPlaylistMetadata;

public class ProductController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final IProductService productService = new ProductServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();
        if ("/product/detail".equals(servletPath)) {
            showDetail(req, resp);
            return;
        }
        showList(req, resp);
    }

    private void showList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int page = parsePage(req.getParameter("page"));
        int pageSize = AppProperties.getInt("app.product.page-size", 6);
        int total = productService.countActive();
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) pageSize));
        if (page > totalPages) {
            page = totalPages;
        }
        req.setAttribute("products", productService.findActive(page, pageSize));
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.getRequestDispatcher("/views/product/list.jsp").include(req, resp);
    }

    private void showDetail(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        Long id = parseId(req.getParameter("id"));
        Product product = productService.findById(id);
        if (product == null) {
            resp.sendRedirect(req.getContextPath() + "/product?message=Track+entry+not+found");
            return;
        }
        req.setAttribute("product", product);
        req.setAttribute("youtubeUrl", SongPlaylistMetadata.youtubeUrlFor(product.getProductName()));
        req.getRequestDispatcher("/views/product/detail.jsp").include(req, resp);
    }

    private int parsePage(String raw) {
        try {
            int page = Integer.parseInt(raw);
            return Math.max(page, 1);
        } catch (Exception e) {
            return 1;
        }
    }

    private Long parseId(String raw) {
        try {
            return Long.parseLong(raw);
        } catch (Exception e) {
            return -1L;
        }
    }
}
