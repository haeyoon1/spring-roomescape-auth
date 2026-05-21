package roomescape.admin.theme;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminThemeControllerTest {

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
    void 테마_정상_생성_확인_테스트() {
        Long storeId = insertStore("강남점");
        insertManager("강남점매니저", "password", storeId);
        String sessionId = login("강남점매니저", "password");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "테마1");
        params.put("description", "테마 설명");
        params.put("imageUrl", "https://example.com/image.jpg");
        params.put("storeId", storeId);

        RestAssured.given().log().all()
            .sessionId(sessionId)
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/admin/themes")
            .then().log().all()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", is("테마1"))
            .body("description", is("테마 설명"))
            .body("imageUrl", is("https://example.com/image.jpg"));
    }

    @Test
    void createTheme_인증되지_않으면_401() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "테마1");
        params.put("description", "테마 설명");
        params.put("imageUrl", "https://example.com/image.jpg");

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/admin/themes")
            .then().log().all()
            .statusCode(401);
    }

    @Test
    void createTheme_일반_유저면_403() {
        insertUser("일반유저", "password");
        String sessionId = login("일반유저", "password");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "테마1");
        params.put("description", "테마 설명");
        params.put("imageUrl", "https://example.com/image.jpg");
        params.put("storeId", 1L);

        RestAssured.given().log().all()
            .sessionId(sessionId)
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/admin/themes")
            .then().log().all()
            .statusCode(403);
    }

    @Test
    void createTheme_다른_매장_매니저면_403() {
        Long gangnam = insertStore("강남점");
        Long hongdae = insertStore("홍대점");
        insertManager("홍대점매니저", "password", hongdae);
        String sessionId = login("홍대점매니저", "password");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "테마1");
        params.put("description", "테마 설명");
        params.put("imageUrl", "https://example.com/image.jpg");
        params.put("storeId", gangnam);

        RestAssured.given().log().all()
            .sessionId(sessionId)
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/admin/themes")
            .then().log().all()
            .statusCode(403);
    }

    @Test
    void createTheme_이름이_비어있는경우_에러_반환_테스트() {
        Long storeId = insertStore("강남점");
        insertManager("강남점매니저", "password", storeId);
        String sessionId = login("강남점매니저", "password");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "");
        params.put("description", "테마 설명");
        params.put("imageUrl", "https://example.com/image.jpg");
        params.put("storeId", storeId);

        RestAssured.given().log().all()
            .sessionId(sessionId)
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/admin/themes")
            .then().log().all()
            .statusCode(400);
    }

    @Test
    void createTheme_중복된_이름인경우_에러_반환_테스트() {
        Long storeId = insertStore("강남점");
        insertManager("강남점매니저", "password", storeId);
        String sessionId = login("강남점매니저", "password");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "테마1");
        params.put("description", "테마 설명");
        params.put("imageUrl", "https://example.com/image.jpg");
        params.put("storeId", storeId);

        RestAssured.given().log().all()
            .sessionId(sessionId)
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/admin/themes")
            .then().log().all()
            .statusCode(201);

        RestAssured.given().log().all()
            .sessionId(sessionId)
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/admin/themes")
            .then().log().all()
            .statusCode(409);
    }

    @Test
    void 테마_전체_조회_정상_동작_테스트() {
        jdbcTemplate.update(
            "INSERT INTO theme (name, description, image_url) VALUES (?, ?, ?)",
            "테마1", "설명1", "https://example.com/1.jpg"
        );
        jdbcTemplate.update(
            "INSERT INTO theme (name, description, image_url) VALUES (?, ?, ?)",
            "테마2", "설명2", "https://example.com/2.jpg"
        );

        RestAssured.given().log().all()
            .when().get("/admin/themes")
            .then().log().all()
            .statusCode(200)
            .body("themes.size()", is(2))
            .body("themes[0].name", is("테마1"))
            .body("themes[1].name", is("테마2"));
    }

    @Test
    void 특정_테마_삭제_정상_동작_테스트() {
        Long storeId = insertStore("강남점");
        insertManager("강남점매니저", "password", storeId);
        String sessionId = login("강남점매니저", "password");
        Long themeId = insertThemeInStore("테마1", storeId);

        RestAssured.given().log().all()
            .sessionId(sessionId)
            .when().delete("/admin/themes/" + themeId)
            .then().log().all()
            .statusCode(204);
    }

    @Test
    void deleteTheme_인증되지_않으면_401() {
        RestAssured.given().log().all()
            .when().delete("/admin/themes/999")
            .then().log().all()
            .statusCode(401);
    }

    @Test
    void deleteTheme_다른_매장_테마면_403() {
        Long gangnam = insertStore("강남점");
        Long hongdae = insertStore("홍대점");
        insertManager("홍대점매니저", "password", hongdae);
        String sessionId = login("홍대점매니저", "password");
        Long themeId = insertThemeInStore("테마1", gangnam);

        RestAssured.given().log().all()
            .sessionId(sessionId)
            .when().delete("/admin/themes/" + themeId)
            .then().log().all()
            .statusCode(403);
    }

    @Test
    void deleteTheme_일반_유저면_403() {
        Long storeId = insertStore("강남점");
        insertUser("일반유저", "password");
        String sessionId = login("일반유저", "password");
        Long themeId = insertThemeInStore("테마1", storeId);

        RestAssured.given().log().all()
            .sessionId(sessionId)
            .when().delete("/admin/themes/" + themeId)
            .then().log().all()
            .statusCode(403);
    }

    @Test
    void deleteTheme_존재하지_않는_id인경우_에러_반환_테스트() {
        Long storeId = insertStore("강남점");
        insertManager("강남점매니저", "password", storeId);
        String sessionId = login("강남점매니저", "password");

        RestAssured.given().log().all()
            .sessionId(sessionId)
            .when().delete("/admin/themes/999")
            .then().log().all()
            .statusCode(404);
    }

    @Test
    void deleteTheme_예약이_존재하는경우_에러_반환_테스트() {
        Long storeId = insertStore("강남점");
        insertManager("강남점매니저", "password", storeId);
        String sessionId = login("강남점매니저", "password");
        Long themeId = insertThemeInStore("테마1", storeId);

        jdbcTemplate.update(
            "INSERT INTO reservation_time (start_at, finish_at) VALUES (?, ?)",
            "10:00", "11:00"
        );
        Long timeId = jdbcTemplate.queryForObject(
            "SELECT id FROM reservation_time WHERE start_at = ?", Long.class, "10:00"
        );
        jdbcTemplate.update(
            "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
            "유저", "2099-01-01", timeId, themeId
        );

        RestAssured.given().log().all()
            .sessionId(sessionId)
            .when().delete("/admin/themes/" + themeId)
            .then().log().all()
            .statusCode(409);
    }

    private String login(String name, String password) {
        Map<String, Object> loginParams = new HashMap<>();
        loginParams.put("name", name);
        loginParams.put("password", password);

        return RestAssured.given()
            .contentType(ContentType.JSON)
            .body(loginParams)
            .when().post("/login")
            .then().statusCode(200)
            .extract().sessionId();
    }

    private Long insertStore(String name) {
        jdbcTemplate.update("INSERT INTO store (name) VALUES (?)", name);
        return jdbcTemplate.queryForObject(
            "SELECT id FROM store WHERE name = ?", Long.class, name
        );
    }

    private void insertUser(String name, String password) {
        jdbcTemplate.update(
            "INSERT INTO users (name, password) VALUES (?, ?)",
            name, passwordEncoder.encode(password)
        );
    }

    private void insertManager(String name, String password, Long storeId) {
        jdbcTemplate.update(
            "INSERT INTO users (name, password, role, store_id) VALUES (?, ?, 'MANAGER', ?)",
            name, passwordEncoder.encode(password), storeId
        );
    }

    private Long insertThemeInStore(String name, Long storeId) {
        jdbcTemplate.update(
            "INSERT INTO theme (name, description, image_url, store_id) VALUES (?, ?, ?, ?)",
            name, "설명", "https://example.com/image.jpg", storeId
        );
        return jdbcTemplate.queryForObject(
            "SELECT id FROM theme WHERE name = ?", Long.class, name
        );
    }
}