package hotspot.user.common.crpyto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SubscriptionKeyLookup {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public Optional<SubscriptionKeyInfo> findKeyInfoBySubId(Long subId) {
        String sql = """
                SELECT s.sub_id, sk.encrypted_dek, sk.kek_key_id
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

    public Map<Long, SubscriptionKeyInfo> findKeyInfosBySubIds(List<Long> subIds) {
        if (subIds == null || subIds.isEmpty()) {
            return Map.of();
        }

        String sql = """
                SELECT s.sub_id, sk.encrypted_dek, sk.kek_key_id
                FROM subscription s
                JOIN subscription_key sk
                  ON sk.bucket_id = s.phone_key_bucket_id
                 AND sk.key_version = s.phone_key_version
                WHERE s.sub_id IN (:subIds)
                  AND s.is_deleted = false
                """;

        MapSqlParameterSource params = new MapSqlParameterSource("subIds", subIds);

        return namedParameterJdbcTemplate.query(sql, params, rs -> {
            Map<Long, SubscriptionKeyInfo> keyInfoBySubId = new LinkedHashMap<>();
            while (rs.next()) {
                keyInfoBySubId.put(rs.getLong("sub_id"), mapKeyInfo(rs));
            }
            return keyInfoBySubId;
        });
    }

    private SubscriptionKeyInfo mapKeyInfo(ResultSet rs, int rowNum) throws SQLException {
        return mapKeyInfo(rs);
    }

    private SubscriptionKeyInfo mapKeyInfo(ResultSet rs) throws SQLException {
        return new SubscriptionKeyInfo(
                rs.getString("encrypted_dek"),
                rs.getString("kek_key_id")
        );
    }
}
