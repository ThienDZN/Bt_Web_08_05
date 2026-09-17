package vn.iotstar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class Assignment08Exercise05GraphqlApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Assignment08Exercise05GraphqlApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Assignment08Exercise05GraphqlApplication.class);
    }
}
