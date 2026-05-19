package roomescape.domain.user;

import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;

public class User {

    private final Long id;
    private final String name;
    private final String password;

    private User(Long id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
    }

    public static User of(Long id, String name, String password) {
        return new User(id, name, password);
    }

    public void validatePassword(String inputPassword) {
        if (!password.equals(inputPassword)) {
            throw new RoomescapeException(ErrorCode.LOGIN_FAILED);
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }
}
