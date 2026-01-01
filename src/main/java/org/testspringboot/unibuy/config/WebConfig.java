package org.testspringboot.unibuy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.testspringboot.unibuy.interceptor.AuthenticationInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;

    @Value("${file.upload.path:./uploads/}")
    private String uploadPath;

    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/user/login", "/user/register", "/user/merchant/register", "/files/**", "/error");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /files/** to local file system (using absolute path)
        String absPath = new File(uploadPath).getAbsolutePath();
        if (!absPath.endsWith(File.separator)) {
            absPath += File.separator;
        }
        // Fix for Windows paths in Spring Boot
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            absPath = absPath.replace("\\", "/");
        }
        
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:///" + absPath);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
