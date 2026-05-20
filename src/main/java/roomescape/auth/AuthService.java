package roomescape.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User login(LoginRequest request) {
        User user = userRepository.findByName(request.name())
            .orElseThrow(() -> new RoomescapeException(ErrorCode.LOGIN_FAILED));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RoomescapeException(ErrorCode.LOGIN_FAILED);
        }
        return user;
    }

    public User signup(SignupRequest request) {
        if (userRepository.existsByName(request.name())) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_USER_NAME);
        }
        String encodedPassword = passwordEncoder.encode(request.password());
        return userRepository.save(User.of(null, request.name(), encodedPassword));
    }
}
