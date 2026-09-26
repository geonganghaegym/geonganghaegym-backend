package com.junghaebom.geonganghaegym.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategySnakeCaseImpl;

/**
 * 따옴표를 친 식별자도 snake_case로 바꾼다.
 * Hibernate 7의 기본 snake_case 전략은 따옴표 친 식별자를 그대로 두는데, 이 프로젝트는 globally_quoted_identifiers로
 * 모든 식별자에 따옴표를 쳐서 변환이 통째로 빠지고 Member·userId 같은 테이블·컬럼이 새로 생겼다(Boot 4 업그레이드 사고).
 * Boot 3(Hibernate 6)처럼 따옴표는 유지하고 이름만 바꾼다.
 */
public class QuotedSnakeCaseNamingStrategy extends PhysicalNamingStrategySnakeCaseImpl {

	@Override
	protected Identifier quotedIdentifier(Identifier quotedName) {
		return Identifier.toIdentifier(unquotedIdentifier(quotedName).getText(), true);
	}
}
