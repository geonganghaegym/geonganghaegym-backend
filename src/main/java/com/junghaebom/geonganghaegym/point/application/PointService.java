package com.junghaebom.geonganghaegym.point.application;

import static com.junghaebom.geonganghaegym.common.error.ErrorCode.*;
import static com.junghaebom.geonganghaegym.course.domain.CourseStatus.*;
import static com.junghaebom.geonganghaegym.member.domain.MemberType.*;
import static com.junghaebom.geonganghaegym.point.domain.Calculation.*;
import static com.junghaebom.geonganghaegym.point.domain.PointType.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.junghaebom.geonganghaegym.common.CustomPaging;
import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.course.application.CourseService;
import com.junghaebom.geonganghaegym.course.presentation.dto.CourseDto;
import com.junghaebom.geonganghaegym.course.domain.CourseStatus;
import com.junghaebom.geonganghaegym.course.repository.CourseRepository;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.repository.MemberRepository;
import com.junghaebom.geonganghaegym.point.presentation.dto.PointHistoryDto;
import com.junghaebom.geonganghaegym.point.presentation.dto.TempRankDto;
import com.junghaebom.geonganghaegym.point.presentation.dto.out.PointDto;
import com.junghaebom.geonganghaegym.point.domain.Calculation;
import com.junghaebom.geonganghaegym.point.domain.Point;
import com.junghaebom.geonganghaegym.point.domain.PointType;
import com.junghaebom.geonganghaegym.point.repository.PointRepository;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.MyReservation;
import com.junghaebom.geonganghaegym.schedule.repository.student.StudentScheduleRepository;
import com.junghaebom.geonganghaegym.trainer.domain.TrainerMemberMapping;
import com.junghaebom.geonganghaegym.trainer.respository.TrainerMemberMappingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PointService {

	private final TrainerMemberMappingRepository mappingRepository;
	private final PointRepository pointRepository;
	private final MemberRepository memberRepository;
	private final StudentScheduleRepository studentScheduleRepository;
	private final CourseRepository courseRepository;
	private final CourseService courseService;

	public void updateMemberRank() {
		List<Long> trainerIds = mappingRepository.findAllTrainerIds();
		List<TrainerMemberMapping> mappings;
		for (Long trainerId : trainerIds) {
			mappings = mappingRepository.findAllByTrainerId(trainerId);
			List<Long> memberIds = mappings.stream().map(m -> m.getMember().getId()).toList();
			List<TempRankDto> ranks = pointRepository.calculateRank(memberIds)
				.stream()
				.toList()
				.stream()
				.map(obj -> new TempRankDto(((Long)obj[0]).intValue(), (Long)obj[1], ((BigDecimal)obj[2]).intValue()))
				.collect(Collectors.toList());

			log.info("[랭킹 산정] trainerId: {}, ranks: {}", trainerId, ranks);

			for (TrainerMemberMapping thisMember : mappings) {
				List<TempRankDto> thisRankDto = ranks.stream()
					.filter(r -> r.memberId().equals(thisMember.getMember().getId()))
					.toList();
				thisMember.changeLastMonthRanking(thisMember.getRanking());
				thisMember.changeRanking(thisRankDto.isEmpty() ? 999 : thisRankDto.get(0).ranking());
			}
		}
	}

	public void updatePoint(Long memberId, PointType type, Calculation calculation, int point) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

		//수강권 잔여 횟수가 0 && 마지막 PT일자 지난 경우 포인트 미지급
		if (checkInvalidCourse(memberId, type, member))
			return;

		if (PLUS == calculation) {
			LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0));
			LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59));
			long cnt = pointRepository.countByMemberIdAndTypeAndCalculationAndCreatedAtBetween(memberId, type,
				calculation, start, end);
			if (0 < cnt)
				return;
		}
		pointRepository.save(Point.create(member, type, calculation, point));
	}

	private boolean checkInvalidCourse(Long memberId, PointType type, Member member) {
		if (DIET == type || WORKOUT == type) {
			CourseDto usingCourse = courseService.getNowUsingCourse(member.getId());
			CourseStatus courseStatus = courseService.getCourseStatus(usingCourse);
			MyReservation myNextReservation = studentScheduleRepository.findMyNextReservation(memberId);
			return !USING.equals(courseStatus) && myNextReservation == null;
		}
		return false;
	}

	public CustomPaging getPoint(Long memberId, String searchDate, Pageable pageable) {
		memberRepository.findByIdAndMemberTypeAndDelYnFalse(memberId, STUDENT)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
		Page<Point> histories = pointRepository.getPoint(memberId, searchDate, pageable);
		int monthPoint = pointRepository.getPointOfSearchMonth(memberId, searchDate);
		int totalPoint = pointRepository.getTotalPoint(memberId, searchDate);

		List<PointHistoryDto> content = histories.map(PointHistoryDto::from).stream().toList();
		CustomPaging customPaging = new CustomPaging<>(content, histories.getPageable().getPageNumber(),
			histories.getPageable().getPageSize(), histories.getTotalPages(), histories.getTotalElements(),
			histories.isLast());
		customPaging.setMainData(PointDto.create(searchDate, monthPoint, totalPoint));
		return customPaging;
	}
}
