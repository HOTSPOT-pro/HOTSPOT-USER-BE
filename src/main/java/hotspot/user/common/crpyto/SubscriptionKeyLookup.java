package hotspot.user.common.crpyto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SubscriptionKeyLookup {

    private final JdbcTemplate jdbcTemplate;

    public Optional<SubscriptionKeyInfo> findKeyInfoBySubId(Long subId) {
        String sql = """
                SELECT sk.encrypted_dek, sk.kek_key_id
                FROM subscription s
                JOIN subscription_key sk
                  ON sk.bucket_id = s.phone_key_bucket_id
                 AND sk.key_version = s.phone_key_version
                WHERE s.sub_id = ?
                  AND s.is_deleted = false
                LIMIT 1
                """;

        return jdbcTemplate.query(sql, this::mapKeyInfo, subId)
                .stream()
                .findFirst();
    }

    private SubscriptionKeyInfo mapKeyInfo(ResultSet rs, int rowNum) throws SQLException {
        return new SubscriptionKeyInfo(
                rs.getString("encrypted_dek"),
                rs.getString("kek_key_id")
        );
    }
}
