package roomescape.domain.user;

import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<User> rowMapper = (rs, rowNum) -> User.of(
        rs.getLong("id"),
        rs.getString("name"),
        rs.getString("password")
    );

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("users")
            .usingGeneratedKeyColumns("id");
    }

    public User save(User user) {
        SqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("name", user.getName())
            .addValue("password", user.getPassword());
        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return User.of(id, user.getName(), user.getPassword());
    }

    public Optional<User> findById(Long id) {
        String query = "SELECT id, name, password FROM users WHERE id = ?";
        return jdbcTemplate.query(query, rowMapper, id).stream().findFirst();
    }

    public Optional<User> findByName(String name) {
        String query = "SELECT id, name, password FROM users WHERE name = ?";
        return jdbcTemplate.query(query, rowMapper, name).stream().findFirst();
    }

    public boolean existsByName(String name) {
        String query = "SELECT COUNT(*) FROM users WHERE name = ?";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, name);
        return count != null && count > 0;
    }
}
