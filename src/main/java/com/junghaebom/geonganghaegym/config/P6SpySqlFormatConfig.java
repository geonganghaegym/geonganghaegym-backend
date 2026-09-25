package com.junghaebom.geonganghaegym.config;

import static com.p6spy.engine.logging.Category.*;
import static com.p6spy.engine.spy.P6SpyOptions.*;
import static java.util.Locale.*;
import static org.hibernate.engine.jdbc.internal.FormatStyle.*;
import static org.springframework.util.StringUtils.*;

import org.springframework.context.annotation.Configuration;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;

import jakarta.annotation.PostConstruct;

@Configuration
public class P6SpySqlFormatConfig implements MessageFormattingStrategy {

	private static final String CREATE = "create";
	private static final String ALTER = "alter";
	private static final String COMMENT = "comment";

	@PostConstruct
	public void setLogMessageFormat() {
		getActiveInstance().setLogMessageFormat(this.getClass().getName());
	}

	@Override
	public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared,
		String sql, String url) {
		sql = applySqlFormat(category, sql);

		return hasText(sql) ? buildFormattedMessage(sql, connectionId, elapsed) : "";
	}

	private String applySqlFormat(String category, String sql) {
		if (!hasText(sql) || !STATEMENT.getName().equals(category)) {
			return sql;
		}

		String trimmedSQL = sql.trim().toLowerCase(ROOT);
		if (trimmedSQL.startsWith(CREATE) || trimmedSQL.startsWith(ALTER) || trimmedSQL.startsWith(COMMENT)) {
			return DDL.getFormatter().format(sql);
		} else {
			return BASIC.getFormatter().format(sql);
		}
	}

	private String buildFormattedMessage(String formattedSql, int connectionId, long elapsed) {
		return String.format("%s%s", formattedSql, formatConnectionInfo(connectionId, elapsed));
	}

	private String formatConnectionInfo(int connectionId, long elapsed) {
		return String.format("""
			
			
			\tConnection ID: %d | Execution Time: %d ms
			
			===========================================================================
			""", connectionId, elapsed);
	}
}
