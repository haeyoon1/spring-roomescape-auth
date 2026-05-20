package roomescape.auth;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.domain.user.UserRepository;

@Configuration
public class AuthenticationPrincipalConfig implements WebMvcConfigurer {

    private final UserRepository userRepository;
    private final AuthenticationResolver authenticationResolver;

    public AuthenticationPrincipalConfig(
        UserRepository userRepository,
        AuthenticationResolver authenticationResolver
    ) {
        this.userRepository = userRepository;
        this.authenticationResolver = authenticationResolver;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginCheckInterceptor(authenticationResolver));
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginUserArgumentResolver(authenticationResolver, userRepository));
    }
}
