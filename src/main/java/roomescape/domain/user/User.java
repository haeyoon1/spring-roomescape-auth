package roomescape.domain.user;

public class User {

    private final Long id;
    private final String name;
    private final String password;
    private final Role role;
    private final Long storeId;

    private User(Long id, String name, String password, Role role, Long storeId) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.role = role;
        this.storeId = storeId;
    }

    public static User of(Long id, String name, String password, Role role, Long storeId) {
        return new User(id, name, password, role, storeId);
    }

    public static User of(Long id, String name, String password) {
        return new User(id, name, password, Role.USER, null);
    }

    public boolean isManagerOf(Long storeId) {
        if (storeId == null) {
            return false;
        }
        return this.role == Role.MANAGER && storeId.equals(this.storeId);
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

    public Role getRole() {
        return role;
    }

    public Long getStoreId() {
        return storeId;
    }
}
