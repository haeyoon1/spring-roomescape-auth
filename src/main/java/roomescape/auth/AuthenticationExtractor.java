package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

public interface AuthenticationExtractor {

    Optional<Long> extract(HttpServletRequest request);
}
