package com.junghaebom.geonganghaegym.diet.application;

import static com.junghaebom.geonganghaegym.common.error.ErrorCode.*;
import static com.junghaebom.geonganghaegym.diet.domain.DietType.*;
import static com.junghaebom.geonganghaegym.member.domain.MemberType.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.junghaebom.geonganghaegym.common.CustomPaging;
import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.common.redis.RedisService;
import com.junghaebom.geonganghaegym.diet.presentation.dto.DietDetailDto;
import com.junghaebom.geonganghaegym.diet.presentation.dto.DietDto;
import com.junghaebom.geonganghaegym.diet.presentation.dto.DietFileDto;
import com.junghaebom.geonganghaegym.diet.presentation.dto.in.DietAddCommand;
import com.junghaebom.geonganghaegym.diet.presentation.dto.in.DietAddCommandAtHome;
import com.junghaebom.geonganghaegym.diet.presentation.dto.in.DietUpdateCommand;
import com.junghaebom.geonganghaegym.diet.presentation.dto.out.DietUploadDaysResult;
import com.junghaebom.geonganghaegym.diet.domain.Diet;
import com.junghaebom.geonganghaegym.diet.domain.DietFiles;
import com.junghaebom.geonganghaegym.diet.domain.DietLike;
import com.junghaebom.geonganghaegym.diet.domain.DietLikePK;
import com.junghaebom.geonganghaegym.diet.domain.DietType;
import com.junghaebom.geonganghaegym.diet.repository.DietCommentRepository;
import com.junghaebom.geonganghaegym.diet.repository.DietFileRepository;
import com.junghaebom.geonganghaegym.diet.repository.DietLikeRepository;
import com.junghaebom.geonganghaegym.diet.repository.DietRepository;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.repository.MemberRepository;
import com.junghaebom.geonganghaegym.trainer.domain.TrainerMemberMapping;
import com.junghaebom.geonganghaegym.trainer.respository.TrainerMemberMappingRepository;
import com.junghaebom.geonganghaegym.file.application.LocalFileStorageService;
import com.junghaebom.geonganghaegym.workout.application.FileService;
import com.junghaebom.geonganghaegym.workout.presentation.dto.in.RegisterFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DietService {

	private final DietRepository dietRepository;
	private final DietLikeRepository dietLikeRepository;
	private final DietFileRepository dietFileRepository;
	private final TrainerMemberMappingRepository mappingRepository;
	private final FileService fileService;
	private final LocalFileStorageService fileStorageService;
	private final MemberRepository memberRepository;
	private final RedisService redisService;
	private final DietCommentRepository commentRepository;

	public DietDto getTodayDiet(Long memberId) {
		Diet diet = dietRepository.getTodayDiet(memberId);
		if (diet == null)
			return new DietDto();
		DietDto dietDto = DietDto.from(diet);
		if (dietLikeRepository.findById(DietLikePK.create(diet, diet.getMember())).isPresent()) {
			dietDto = dietDto.withLiked(true);
		}
		dietDto = setDietFile(dietDto, List.of(diet.getDietId()));
		return dietDto;
	}

	public CustomPaging<DietDto> getDiet(Long loginMemberId, Long memberId, Pageable pageable, String searchDate) {
		Page<DietDto> pageDtos = dietRepository.getDietOfMonth(loginMemberId, memberId, pageable, searchDate);
		List<DietDto> dietDtos = pageDtos.stream().toList();
		List<Long> ids = dietDtos.stream().map(DietDto::dietId).collect(Collectors.toList());
		dietDtos = setDietFile(dietDtos, ids);
		return new CustomPaging<>(dietDtos, pageDtos.getPageable().getPageNumber(),
			pageDtos.getPageable().getPageSize(), pageDtos.getTotalPages(), pageDtos.getTotalElements(),
			pageDtos.isLast());
	}

	public void likeDiet(Member member, Long dietId) {
		Diet diet = dietRepository.findByDietIdAndDelYnFalse(dietId)
			.orElseThrow(() -> new CustomException(DIET_NOT_FOUND));
		DietLikePK likePk = DietLikePK.create(diet, member);
		dietLikeRepository.findById(likePk).ifPresent(i -> {
			throw new CustomException(LIKE_ALREADY_EXISTS);
		});
		dietLikeRepository.save(DietLike.from(likePk));
		diet.updateLikeCnt(dietLikeRepository.getLikeCnt(diet.getDietId()));
	}

	public void deleteLikeDiet(Member member, Long dietId) {
		Diet diet = dietRepository.findByDietIdAndDelYnFalse(dietId)
			.orElseThrow(() -> new CustomException(DIET_NOT_FOUND));
		dietLikeRepository.delete(DietLike.from(DietLikePK.create(diet, member)));
		diet.updateLikeCnt(dietLikeRepository.getLikeCnt(diet.getDietId()));
	}

	public DietDto addDietAtHome(Member member, DietAddCommandAtHome command) {
		if (!LocalDate.now().toString().equals(command.eatDate())) {
			throw new CustomException(DATE_NOT_VALID);
		}
		Long memberId = member.getId();
		DietType requestType = command.type();
		String requestFileUrl = command.file();

		Diet diet = dietRepository.getTodayDiet(memberId);

		Member trainer = getMappedTrainer(memberId);
		if (ObjectUtils.isEmpty(diet))
			diet = dietRepository.save(Diet.create(member, trainer));

		diet.changeTrainer(trainer);

		//기존 사진 삭제
		List<DietFiles> files = diet.getDietFiles().stream().filter(f -> requestType.equals(f.getType())).toList();
		if (!ObjectUtils.isEmpty(files)) {
			fileService.deleteDietFile(files.get(0).getFileName());
			diet.deleteFile(requestType);
		}
		//사진 첨부
		if (!command.fast() && !ObjectUtils.isEmpty(requestFileUrl) && isTempFile(requestFileUrl)) {
			RegisterFile result = fileService.moveDirTempToOrigin("diet/", requestFileUrl);
			dietFileRepository.save(DietFiles.create(diet, result.fileUrl(), requestType));

		}

		diet.changeEatDate(LocalDate.parse(command.eatDate()));
		diet.changeFast(command.type(), command.fast());
		DietDto dietDto = DietDto.from(diet);
		dietDto = setDietFile(dietDto, List.of(diet.getDietId()));

		if (isClean(dietDto) && commentNotExists(diet))
			diet.deleteDiet();
		log.info("[홈에서 식단 등록] member: {}, diet: {}", member, diet);
		return dietDto;
	}

	private boolean commentNotExists(Diet diet) {
		return commentRepository.countByDietAndDelYnFalse(diet) == 0L;
	}

	private boolean isClean(DietDto dietDto) {
		DietDetailDto breakfast = dietDto.breakfast();
		DietDetailDto lunch = dietDto.lunch();
		DietDetailDto dinner = dietDto.dinner();
		return (!breakfast.fast() && breakfast.dietFile() == null)
			&& (!lunch.fast() && lunch.dietFile() == null)
			&& (!dinner.fast() && dinner.dietFile() == null);
	}

	public DietDto getDietDetail(Long loginMemberId, Long dietId) {
		DietDto dietDto = dietRepository.getDietById(loginMemberId, dietId);
		if (dietDto == null)
			throw new CustomException(DIET_NOT_FOUND);
		dietDto = setDietFile(dietDto, List.of(dietId));
		return dietDto;
	}

	private DietDto setDietFile(DietDto dietDto, List<Long> ids) {
		List<DietFiles> files = dietRepository.getDietFile(ids);
		List<DietFileDto> filesDto = files.stream().map(DietFileDto::from).collect(Collectors.toList());
		return dietDto.withDietFiles(filesDto);
	}

	private List<DietDto> setDietFile(List<DietDto> dietDtos, List<Long> ids) {
		List<DietFiles> files = dietRepository.getDietFile(ids);
		return dietDtos.stream().map(d -> {
			List<DietFileDto> thisFiles = files.stream().map(DietFileDto::from)
				.filter(f -> f.dietId().equals(d.dietId())).collect(Collectors.toList());
			return d.withDietFiles(thisFiles);
		}).collect(Collectors.toList());
	}

	public void deleteDiet(Member member, Long dietId) {
		Diet diet = dietRepository.findByDietIdAndMemberIdAndDelYnFalse(dietId, member.getId())
			.orElseThrow(() -> new CustomException(DIET_NOT_FOUND));
		diet.deleteDiet();
		dietLikeRepository.deleteLikeByDietId(dietId);
		diet.getDietFiles().forEach(file -> fileService.deleteDietFile(getFileName(file.getFileUrl())));
		log.info("[식단 삭제] member: {}, diet: {}", member, diet);
	}

	public DietDto addDiet(Member member, DietAddCommand command) {
		List<String> uploadDays = dietRepository.getDietUploadDays(member.getId(), null, null);
		if (uploadDays.contains(command.eatDate()))
			throw new CustomException(DIET_ALREADY_EXISTS);

		Member trainer = getMappedTrainer(member.getId());
		Diet diet = dietRepository.save(Diet.create(member, trainer,
			command.breakfastFast(),
			command.lunchFast(),
			command.dinnerFast(),
			LocalDate.parse(command.eatDate())));
		uploadNewFiles(diet, command);
		DietDto dietDto = DietDto.from(diet);
		dietDto = setDietFile(dietDto, List.of(diet.getDietId()));
		log.info("[식단 등록] member: {}, diet: {}", member, diet);
		return dietDto;
	}

	private @Nullable Member getMappedTrainer(Long memberId) {
		TrainerMemberMapping mapping = mappingRepository.findTop1ByMemberIdOrderByCreatedAtDesc(memberId).orElse(null);
		return mapping == null ? null : mapping.getTrainer();
	}

	public DietDto updateDiet(Member member, Long dietId, DietUpdateCommand command) {
		Diet diet = dietRepository.findByDietIdAndMemberIdAndDelYnFalse(dietId, member.getId())
			.orElseThrow(() -> new CustomException(DIET_NOT_FOUND));

		diet.changeTrainer(getMappedTrainer(member.getId()));
		diet.changeFast(command.breakfastFast(), command.lunchFast(), command.dinnerFast());
		deleteOldFiles(diet, command);
		uploadNewFiles(diet, command);

		DietDto dietDto = DietDto.from(diet);
		dietDto = setDietFile(dietDto, List.of(diet.getDietId()));
		if (isClean(dietDto))
			throw new CustomException(DIET_NOT_VALID);
		log.info("[식단 수정] member: {}, diet: {}", member, diet);
		return dietDto;
	}

	private void uploadNewFiles(Diet diet, DietUpdateCommand command) {
		//아침 파일
		if (!ObjectUtils.isEmpty(command.breakfastFile()) && isTempFile(command.breakfastFile())) {
			if (command.breakfastFast()) { //단식
				List<DietFiles> files = diet.getDietFiles()
					.stream()
					.filter(f -> BREAKFAST.equals(f.getType()))
					.toList();
				if (!ObjectUtils.isEmpty(files))
					fileService.deleteDietFile(files.get(0).getFileName());

			} else { //사진있음
				RegisterFile result = fileService.moveDirTempToOrigin("diet/", command.breakfastFile());
				dietFileRepository.save(DietFiles.create(diet, result.fileUrl(), BREAKFAST));
			}
		}

		//점심 파일
		if (!ObjectUtils.isEmpty(command.lunchFile()) && isTempFile(command.lunchFile())) {
			if (command.lunchFast()) { //단식
				List<DietFiles> files = diet.getDietFiles().stream().filter(f -> LUNCH.equals(f.getType())).toList();
				if (!ObjectUtils.isEmpty(files))
					fileService.deleteDietFile(files.get(0).getFileName());

			} else { //사진있음
				RegisterFile result = fileService.moveDirTempToOrigin("diet/", command.lunchFile());
				dietFileRepository.save(DietFiles.create(diet, result.fileUrl(), LUNCH));
			}
		}

		//저녁 파일
		if (!ObjectUtils.isEmpty(command.dinnerFile()) && isTempFile(command.dinnerFile())) {
			if (command.dinnerFast()) { //단식
				List<DietFiles> files = diet.getDietFiles().stream().filter(f -> DINNER.equals(f.getType())).toList();
				if (!ObjectUtils.isEmpty(files))
					fileService.deleteDietFile(files.get(0).getFileName());

			} else { //사진있음
				RegisterFile result = fileService.moveDirTempToOrigin("diet/", command.dinnerFile());
				dietFileRepository.save(DietFiles.create(diet, result.fileUrl(), DINNER));
			}
		}
	}

	private void uploadNewFiles(Diet diet, DietAddCommand command) {
		uploadNewFiles(diet, new DietUpdateCommand(
			command.breakfastFile(), command.lunchFile(), command.dinnerFile(),
			command.breakfastFast(), command.lunchFast(), command.dinnerFast()
		));
	}

	private boolean isTempFile(String fileUrl) {
		String filePath = fileStorageService.extractFilePath(fileUrl);
		return filePath.startsWith("temp/");
	}

	private void deleteOldFiles(Diet diet, DietUpdateCommand command) {
		Set<String> oldFileNames = diet.getDietFiles().stream()
			.map(DietFiles::getFileName).collect(Collectors.toSet());
		if (command.breakfastFile() != null && !command.breakfastFast())
			oldFileNames.remove(getFileName(command.breakfastFile()));
		if (command.lunchFile() != null && !command.lunchFast())
			oldFileNames.remove(getFileName(command.lunchFile()));
		if (command.dinnerFile() != null && !command.dinnerFast())
			oldFileNames.remove(getFileName(command.dinnerFile()));
		Set<String> deleteFileNames = diet.getDietFiles().stream()
			.map(DietFiles::getFileName)
			.filter(oldFileNames::contains)
			.collect(Collectors.toSet());

		diet.getDietFiles().stream()
			.filter(f -> deleteFileNames.contains(f.getFileName()))
			.forEach(f -> {
				fileService.deleteDietFile(f.getFileName());
				f.deleteDietFile();
			});
	}

	public CustomPaging<DietDto> getDietByTrainer(Long trainerId, Pageable pageable, String searchDate) {
		Member trainer = memberRepository.findByIdAndMemberTypeAndDelYnFalse(trainerId, TRAINER)
			.orElseThrow(() -> new CustomException(TRAINER_NOT_FOUND));
		Page<Diet> pageDtos = dietRepository.getDietByTrainer(trainer, pageable, searchDate);
		List<DietDto> dietDtos = setFeedbackChecked(trainerId, pageDtos);
		List<Long> ids = dietDtos.stream().map(DietDto::dietId).collect(Collectors.toList());
		dietDtos = setDietFile(dietDtos, ids);
		return new CustomPaging<>(dietDtos, pageDtos.getPageable().getPageNumber(),
			pageDtos.getPageable().getPageSize(), pageDtos.getTotalPages(), pageDtos.getTotalElements(),
			pageDtos.isLast());
	}

	private List<DietDto> setFeedbackChecked(Long trainerId, Page<Diet> pageDtos) {
		return pageDtos.map(diet -> {
			DietDto dto = DietDto.from(diet);
			Long feedbackCnt = commentRepository.countByDietAndMemberIdAndDelYnFalse(diet, trainerId);
			return dto.withFeedbackChecked(0 < feedbackCnt);
		}).stream().toList();
	}

	private String getFileName(String url) {
		String[] arr = url.split("/");
		return arr[arr.length - 1];
	}

	public DietUploadDaysResult getDietUploadDays(Long memberId, LocalDate startDate, LocalDate endDate) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
		List<String> days = dietRepository.getDietUploadDays(memberId, startDate, endDate);
		return DietUploadDaysResult.create(member.getDietNoticeStatus(), days);
	}

}
