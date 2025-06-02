package com.wooteco.wiki.global.config;

import com.wooteco.wiki.global.config.resolver.AuthArgumentResolver;
import com.wooteco.wiki.global.config.interceptor.LoginCheckInterceptor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final AuthArgumentResolver authArgumentResolver;
    private final LoginCheckInterceptor loginCheckInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(loginCheckInterceptor)
//                .addPathPatterns("/**")
//                .excludePathPatterns("/login/**", "/", "/themes/ranking", "/**/*.html", "/**/*.js", "/**/*.css");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:3000", "https://dev.crew-wiki.site", "https://crew-wiki.site",
                        "https://www.crew-wiki.site")
                .allowCredentials(true)
                .allowedMethods("*");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
//        resolvers.add(authArgumentResolver);
    }
}
