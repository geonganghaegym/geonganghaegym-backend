package com.junghaebom.geonganghaegym.diet.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.junghaebom.geonganghaegym.diet.presentation.dto.DietDto;
import com.junghaebom.geonganghaegym.diet.domain.Diet;
import com.junghaebom.geonganghaegym.diet.domain.DietFiles;
import com.junghaebom.geonganghaegym.member.domain.Member;

public interface DietRepositoryCustom {

	Page<DietDto> getDietOfMonth(Long loginMemberId, Long memberId, Pageable pageable, String searchDate);

	List<DietFiles> getDietFile(List<Long> ids);

	Page<Diet> getDietByTrainer(Member trainer, Pageable pageable, String searchDate);

	Diet getTodayDiet(Long memberId);

	List<String> getDietUploadDays(Long memberId, LocalDate startDate, LocalDate endDate);

	DietDto getDietById(Long loginMemberId, Long dietId);
}
