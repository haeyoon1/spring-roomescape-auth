package roomescape.domain.store;

public class Store {

    private final Long id;
    private final String name;

    private Store(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Store of(Long id, String name) {
        return new Store(id, name);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
