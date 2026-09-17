package vn.iotstar.config;

import java.util.EnumSet;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.iotstar.controller.AuthController;
import vn.iotstar.controller.HomeController;
import vn.iotstar.controller.ProductController;
import vn.iotstar.controller.ProfileController;
import vn.iotstar.controller.admin.UserController;
import vn.iotstar.controller.common.ImageServlet;
import vn.iotstar.filter.AuthFilter;
import vn.iotstar.filter.Utf8EncodingFilter;
import vn.iotstar.listener.AppBootstrapListener;

@Configuration
public class WebInfrastructureConfig {
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final long MAX_REQUEST_SIZE = 6L * 1024 * 1024;
    private static final int FILE_SIZE_THRESHOLD = 1024 * 1024;

    @Bean
    FilterRegistrationBean<Utf8EncodingFilter> utf8EncodingFilter() {
        FilterRegistrationBean<Utf8EncodingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new Utf8EncodingFilter());
        registration.addUrlPatterns("/*");
        registration.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD));
        registration.setOrder(1);
        return registration;
    }

    @Bean
    FilterRegistrationBean<AuthFilter> authFilter() {
        FilterRegistrationBean<AuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AuthFilter());
        registration.addUrlPatterns("/admin/*");
        registration.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD));
        registration.setOrder(2);
        return registration;
    }

    @Bean
    ServletListenerRegistrationBean<AppBootstrapListener> appBootstrapListener() {
        return new ServletListenerRegistrationBean<>(new AppBootstrapListener());
    }

    @Bean
    ServletRegistrationBean<HomeController> homeControllerServlet() {
        return new ServletRegistrationBean<>(new HomeController(), "/home");
    }

    @Bean
    ServletRegistrationBean<AuthController> authControllerServlet() {
        return new ServletRegistrationBean<>(new AuthController(),
                "/login",
                "/logout",
                "/register",
                "/verify-otp",
                "/forgot-password",
                "/reset-password",
                "/resend-otp");
    }

    @Bean
    ServletRegistrationBean<ImageServlet> imageServlet() {
        return new ServletRegistrationBean<>(new ImageServlet(), "/image");
    }

    @Bean
    ServletRegistrationBean<ProductController> productControllerServlet() {
        return new ServletRegistrationBean<>(new ProductController(), "/product", "/product/detail");
    }

    @Bean
    ServletRegistrationBean<vn.iotstar.controller.admin.ProductController> adminProductControllerServlet() {
        ServletRegistrationBean<vn.iotstar.controller.admin.ProductController> registration =
                new ServletRegistrationBean<>(new vn.iotstar.controller.admin.ProductController(),
                        "/admin/products",
                        "/admin/product/add",
                        "/admin/product/insert",
                        "/admin/product/edit",
                        "/admin/product/update",
                        "/admin/product/delete");
        registration.setMultipartConfig(multipartConfig());
        return registration;
    }

    @Bean
    ServletRegistrationBean<ProfileController> profileControllerServlet() {
        ServletRegistrationBean<ProfileController> registration =
                new ServletRegistrationBean<>(new ProfileController(), "/profile");
        registration.setMultipartConfig(multipartConfig());
        return registration;
    }

    @Bean
    ServletRegistrationBean<UserController> userControllerServlet() {
        return new ServletRegistrationBean<>(new UserController(),
                "/admin/users",
                "/admin/user/add",
                "/admin/user/insert",
                "/admin/user/edit",
                "/admin/user/update",
                "/admin/user/delete");
    }

    private MultipartConfigElement multipartConfig() {
        return new MultipartConfigElement("", MAX_FILE_SIZE, MAX_REQUEST_SIZE, FILE_SIZE_THRESHOLD);
    }
}
