package pe.edu.utec.marketing;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig
        implements WebMvcConfigurer {

    @Value("${app.media-dir}")
    private String mediaDir;


    @Override
    public void addResourceHandlers(
            ResourceHandlerRegistry registry
    ) {

        registry
            .addResourceHandler("/media/**")
            .addResourceLocations(
                Path.of(mediaDir).toAbsolutePath().toUri().toString() + "/"
            );
    }
}
