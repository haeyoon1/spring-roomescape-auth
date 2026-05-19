package roomescape.auth;

import org.springframework.stereotype.Service;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.dto.SignupRequest;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRepository;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User login(LoginRequest request) {
        User user = userRepository.findByName(request.name())
            .orElseThrow(() -> new RoomescapeException(ErrorCode.LOGIN_FAILED));
        user.validatePassword(request.password());
        return user;
    }

    public User signup(SignupRequest request) {
        if (userRepository.existsByName(request.name())) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_USER_NAME);
        }
        return userRepository.save(User.of(null, request.name(), request.password()));
    }
}
