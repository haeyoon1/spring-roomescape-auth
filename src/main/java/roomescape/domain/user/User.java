package roomescape.domain.user;

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
