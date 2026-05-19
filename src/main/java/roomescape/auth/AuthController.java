package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.dto.LoginCheckResponse;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.dto.SignupRequest;
import roomescape.domain.user.User;

@RestController
public class AuthController {

    static final String LOGIN_USER_ID = "loginUserId";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(
        @RequestBody @Valid LoginRequest request,
        HttpServletRequest httpRequest
    ) {
        User user = authService.login(request);
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(LOGIN_USER_ID, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(
        @RequestBody @Valid SignupRequest request,
        HttpServletRequest httpRequest
    ) {
        User user = authService.signup(request);
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(LOGIN_USER_ID, user.getId());
        return ResponseEntity.created(URI.create("/users/" + user.getId())).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<LoginCheckResponse> check(@Auth User user) {
        return ResponseEntity.ok(new LoginCheckResponse(user.getName()));
    }
}
