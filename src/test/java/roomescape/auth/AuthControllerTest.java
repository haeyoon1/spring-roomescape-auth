package roomescape.auth;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class AuthControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 로그인은 401을 반환한다")
    void 로그인_비밀번호_불일치() {
        insertUser("유저1", "password1");

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(Map.of("name", "유저1", "password", "wrongPassword"))
            .when().post("/login")
            .then().log().all()
            .statusCode(401);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 로그인하면 401을 반환한다")
    void 로그인_존재하지_않는_사용자() {
        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(Map.of("name", "없는유저", "password", "password1"))
            .when().post("/login")
            .then().log().all()
            .statusCode(401);
    }

    @Test
    @DisplayName("로그인하지 않은 상태로 인증이 필요한 API를 호출하면 401을 반환한다")
    void 비로그인_상태로_인증필요_API_호출() {
        RestAssured.given().log().all()
            .when().get("/login/check")
            .then().log().all()
            .statusCode(401);
    }

    @Test
    @DisplayName("유효하지 않은 세션 ID로 인증이 필요한 API를 호출하면 401을 반환한다")
    void 잘못된_세션_ID로_인증필요_API_호출() {
        RestAssured.given().log().all()
            .sessionId("invalid-session-id")
            .when().get("/login/check")
            .then().log().all()
            .statusCode(401);
    }

    private void insertUser(String name, String password) {
        jdbcTemplate.update(
            "INSERT INTO users (name, password) VALUES (?, ?)",
            name, passwordEncoder.encode(password)
        );
    }
}
