package fr.alex96x2.admin.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
public class SpaWebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/dashboard/assets/**")
                .addResourceLocations("classpath:/static/assets/");

        registry.addResourceHandler("/dashboard/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(@NonNull String resourcePath, @NonNull Resource location) throws IOException {
                        if (resourcePath.startsWith("api/") || resourcePath.startsWith("assets/")) {
                            return null;
                        }

                        String path = normalizeResourcePath(resourcePath);
                        if (path.isEmpty()) {
                            return location.createRelative("index.html");
                        }

                        Resource resource = location.createRelative(path);
                        if (resource.exists() && resource.isReadable()) {
                            return resource;
                        }
                        return location.createRelative("index.html");
                    }
                });
    }

    private static String normalizeResourcePath(String resourcePath) {
        String path = resourcePath;
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.startsWith("dashboard/")) {
            path = path.substring("dashboard/".length());
        }
        return path;
    }
}
