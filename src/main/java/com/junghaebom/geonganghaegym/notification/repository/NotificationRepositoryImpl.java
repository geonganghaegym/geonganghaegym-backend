package com.junghaebom.geonganghaegym.notification.repository;

import static com.junghaebom.geonganghaegym.notification.domain.QNotification.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.junghaebom.geonganghaegym.notification.presentation.dto.out.NotificationRedDotStatusResult;
import com.junghaebom.geonganghaegym.notification.domain.Notification;
import com.junghaebom.geonganghaegym.notification.domain.NotificationCategory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<Notification> findAllByNotificationType(
		NotificationCategory notificationCategory,
		Long receiverId,
		Pageable pageable) {

		List<Notification> results = queryFactory
			.select(notification)
			.from(notification)
			.where(
				notificationCategoryEq(notificationCategory),
				notification.receiver.id.eq(receiverId)
			)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(notification.id.desc())
			.fetch();

		var totalCount = queryFactory
			.select(notification.count())
			.from(notification)
			.where(
				notificationCategoryEq(notificationCategory),
				notification.receiver.id.eq(receiverId)
			);

		return PageableExecutionUtils.getPage(results, pageable,
			() -> {
				Long count = totalCount.fetchOne();
				return count != null ? count : 0L;
			});
	}

	@Override
	public List<NotificationRedDotStatusResult> findAllRedDotStatus(
		NotificationCategory notificationCategory,
		Long receiverId) {

		return queryFactory
			.select(
				Projections.constructor(
					NotificationRedDotStatusResult.class,
					notification.notificationCategory,
					new CaseBuilder()
						.when(
							new CaseBuilder()
								.when(notification.receiver.id.eq(receiverId).and(notification.isRead.eq(false)))
								.then(1)
								.otherwise(0)
								.sum().gt(0)
						)
						.then(true)
						.otherwise(false)
				)
			)
			.from(notification)
			.where(
				notificationCategoryNq(notificationCategory)
			)
			.groupBy(notification.notificationCategory)
			.fetch();
	}

	@Override
	public boolean findRedDotStatus(Long receiverId) {
		Long count = queryFactory
			.select(notification.count())
			.from(notification)
			.where(
				notification.receiver.id.eq(receiverId),
				notification.isRead.eq(false)
			)
			.fetchOne();
		return count != null && count > 0;
	}

	private BooleanExpression notificationCategoryEq(NotificationCategory notificationCategory) {
		if (ObjectUtils.isEmpty(notificationCategory)) {
			return null;
		}
		return notification.notificationCategory.eq(notificationCategory);
	}

	private BooleanExpression notificationCategoryNq(NotificationCategory notificationCategory) {
		if (ObjectUtils.isEmpty(notificationCategory)) {
			return null;
		}
		return notification.notificationCategory.ne(notificationCategory);
	}
}
