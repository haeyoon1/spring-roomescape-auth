package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(20)
public class SessionAuthenticationExtractor implements AuthenticationExtractor {

    @Override
    public Optional<Long> extract(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return Optional.empty();
        }
        Object attribute = session.getAttribute(AuthController.LOGIN_USER_ID);
        if (attribute instanceof Long userId) {
            return Optional.of(userId);
        }
        return Optional.empty();
    }
}
