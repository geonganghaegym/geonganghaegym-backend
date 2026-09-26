package com.junghaebom.geonganghaegym.config;

import static org.assertj.core.api.Assertions.*;

import org.hibernate.boot.model.naming.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QuotedSnakeCaseNamingStrategyTest {

	private final QuotedSnakeCaseNamingStrategy strategy = new QuotedSnakeCaseNamingStrategy();

	@Test
	@DisplayName("따옴표 친 테이블·컬럼 이름도 snake_case로 바꾸고 따옴표는 유지한다 (Boot 4 업그레이드 사고 회귀)")
	void convertsQuotedIdentifiers() {
		Identifier table = strategy.toPhysicalTableName(Identifier.toIdentifier("TrainerScheduleInfo", true), null);
		Identifier column = strategy.toPhysicalColumnName(Identifier.toIdentifier("userId", true), null);

		assertThat(table.getText()).isEqualTo("trainer_schedule_info");
		assertThat(table.isQuoted()).isTrue();
		assertThat(column.getText()).isEqualTo("user_id");
		assertThat(column.isQuoted()).isTrue();
	}

	@Test
	@DisplayName("따옴표 없는 이름도 snake_case로 바꾼다")
	void convertsUnquotedIdentifiers() {
		assertThat(strategy.toPhysicalColumnName(Identifier.toIdentifier("lastMonthRanking"), null).getText())
			.isEqualTo("last_month_ranking");
	}
}
