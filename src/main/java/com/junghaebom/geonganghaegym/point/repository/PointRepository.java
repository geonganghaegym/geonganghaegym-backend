package com.junghaebom.geonganghaegym.point.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.point.domain.Calculation;
import com.junghaebom.geonganghaegym.point.domain.Point;
import com.junghaebom.geonganghaegym.point.domain.PointType;

public interface PointRepository extends JpaRepository<Point, Long>, PointRepositoryCustom {

	@Query(value = "select RANK() OVER (ORDER BY a.point_sum desc) AS ranking, a.member_id, a.point_sum "
		+
		"from (" +
		"select p.member_id, " +
		"sum(case when p.calculation = 'PLUS' then p.point " +
		"when p.calculation = 'MINUS' then p.point*(-1) end) AS point_sum " +
		"from point p " +
		"where p.created_at between date_format(now() - interval 1 month, '%Y-%m-01 00:00:00') " +
		"and last_day(now() - interval 1 month) + interval 1 day - interval 1 second " +
		"group by p.member_id " +
		"having p.member_id in(:members)) a", nativeQuery = true)
	List<Object[]> calculateRank(List<Long> members);

	long countByMemberIdAndTypeAndCalculationAndCreatedAtBetween(Long memberId, PointType type, Calculation calculation,
		LocalDateTime start, LocalDateTime end);

	@Query(value = "select ifnull(sum(case when p.calculation = 'PLUS' then p.point " +
		"when p.calculation = 'MINUS' then p.point*(-1) end), 0) AS point_sum " +
		"from point p where member_id = :memberId " +
		"and date_format(p.created_at, '%Y-%m') = :searchDate", nativeQuery = true)
	int getPointOfSearchMonth(Long memberId, String searchDate);

	@Query(value = "select ifnull(sum(case when p.calculation = 'PLUS' then p.point " +
		"when p.calculation = 'MINUS' then p.point*(-1) end), 0) AS point_sum " +
		"from point p where member_id = :memberId " +
		"and date_format(p.created_at, '%Y-%m') <= :searchDate", nativeQuery = true)
	int getTotalPoint(Long memberId, String searchDate);

	void deleteByMember(Member member);
}
