package com.junghaebom.geonganghaegym.member.repository;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.junghaebom.geonganghaegym.member.domain.ComplimentaryLoginHistory;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ComplimentaryLoginHistoryRepository {

	private static final String INSERT_SQL = """
		INSERT INTO complimentary_login_history (
			member_id,
			user_id,
			member_type,
			created_at,
			updated_at
		) VALUES (
			:memberId,
			:userId,
			:memberType,
			:createdAt,
			:updatedAt
		)
		""";

	private static final int MYSQL_TABLE_NOT_FOUND_ERROR_CODE = 1146;
	private static final String MYSQL_TABLE_NOT_FOUND_SQL_STATE = "42S02";

	private final NamedParameterJdbcTemplate jdbcTemplate;
	private final AtomicBoolean unavailable = new AtomicBoolean(false);

	public void save(ComplimentaryLoginHistory complimentaryLoginHistory) {
		if (unavailable.get()) {
			return;
		}

		try {
			jdbcTemplate.update(
				INSERT_SQL,
				new MapSqlParameterSource()
					.addValue("memberId", complimentaryLoginHistory.getMemberId())
					.addValue("userId", complimentaryLoginHistory.getUserId())
					.addValue("memberType", complimentaryLoginHistory.getMemberType().name())
					.addValue("createdAt", complimentaryLoginHistory.getCreatedAt())
					.addValue("updatedAt", complimentaryLoginHistory.getUpdatedAt())
			);
		} catch (DataAccessException e) {
			handleSaveFailure(complimentaryLoginHistory, e);
		}
	}

	private void handleSaveFailure(ComplimentaryLoginHistory complimentaryLoginHistory, DataAccessException e) {
		if (isMissingTable(e)) {
			if (unavailable.compareAndSet(false, true)) {
				log.warn("Skip complimentary login history persistence because table is missing.");
			}
			return;
		}

		log.warn(
			"Skip complimentary login history persistence. memberId={}, userId={}",
			complimentaryLoginHistory.getMemberId(),
			complimentaryLoginHistory.getUserId(),
			e
		);
	}

	private boolean isMissingTable(DataAccessException e) {
		Throwable cause = NestedExceptionUtils.getMostSpecificCause(e);
		if (!(cause instanceof SQLException sqlException)) {
			return false;
		}

		return sqlException.getErrorCode() == MYSQL_TABLE_NOT_FOUND_ERROR_CODE
			|| MYSQL_TABLE_NOT_FOUND_SQL_STATE.equals(sqlException.getSQLState());
	}
}
