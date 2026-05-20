package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;

@Component
public class AuthenticationResolver {

    private final List<AuthenticationExtractor> extractors;

    public AuthenticationResolver(List<AuthenticationExtractor> extractors) {
        this.extractors = extractors;
    }

    public Long resolveUserId(HttpServletRequest request) {
        return extractors.stream()
            .map(extractor -> extractor.extract(request))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst()
            .orElseThrow(() -> new RoomescapeException(ErrorCode.UNAUTHORIZED));
    }
}