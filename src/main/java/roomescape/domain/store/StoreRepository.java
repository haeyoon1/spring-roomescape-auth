package roomescape.domain.store;

import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class StoreRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Store> rowMapper = (rs, rowNum) -> Store.of(
        rs.getLong("id"),
        rs.getString("name")
    );

    public StoreRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Store> findById(Long id) {
        String query = "SELECT id, name FROM store WHERE id = ?";
        return jdbcTemplate.query(query, rowMapper, id).stream().findFirst();
    }
}
