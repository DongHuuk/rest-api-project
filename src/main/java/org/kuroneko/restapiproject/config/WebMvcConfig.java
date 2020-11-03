package org.kuroneko.restapiproject.config;

import org.kuroneko.restapiproject.account.AccountFormConverter;
import org.kuroneko.restapiproject.token.JwtTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.StaticResourceLocation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.servlet.config.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtTokenInterceptor interceptor;

    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {
            "classpath:/static/", "classpath:/public/", "classpath:/",
            "classpath:/resources/", "classpath:/META-INF/resources/",
            "classpath:/META-INF/resources/webjars/" };

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        List<String> collect = Arrays.stream(StaticResourceLocation.values())
                .flatMap(StaticResourceLocation::getPatterns)
                .collect(Collectors.toList());

        registry.addResourceHandler("/**").addResourceLocations(collect.stream().toString())
        .addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.interceptor).addPathPatterns("/accounts/**");
        registry.addInterceptor(this.interceptor).addPathPatterns("/community");
        registry.addInterceptor(this.interceptor).addPathPatterns("/community/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods(HttpMethod.POST.name())
                .allowCredentials(false)
                .maxAge(3600);

    }
}
