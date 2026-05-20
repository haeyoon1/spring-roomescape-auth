package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRepository;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;

public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthenticationResolver authenticationResolver;
    private final UserRepository userRepository;

    public LoginUserArgumentResolver(
        AuthenticationResolver authenticationResolver,
        UserRepository userRepository
    ) {
        this.authenticationResolver = authenticationResolver;
        this.userRepository = userRepository;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(Auth.class);
        boolean isUserType = User.class.isAssignableFrom(parameter.getParameterType());

        return hasAnnotation && isUserType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            throw new RoomescapeException(ErrorCode.UNAUTHORIZED);
        }
        Long userId = authenticationResolver.resolveUserId(request);
        return userRepository.findById(userId)
            .orElseThrow(() -> new RoomescapeException(ErrorCode.UNAUTHORIZED));
    }
}
