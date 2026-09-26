package com.junghaebom.geonganghaegym.workout.application;

import static com.junghaebom.geonganghaegym.common.error.ErrorCode.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.junghaebom.geonganghaegym.trainer.application.MemberDataAccessValidator;
import com.junghaebom.geonganghaegym.common.CustomPaging;
import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.member.presentation.dto.MemberDto;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.repository.MemberRepository;
import com.junghaebom.geonganghaegym.workout.presentation.dto.CompletedExerciseDto;
import com.junghaebom.geonganghaegym.workout.presentation.dto.WorkoutHistoryFileDto;
import com.junghaebom.geonganghaegym.workout.presentation.dto.in.HistoryAddCommand;
import com.junghaebom.geonganghaegym.workout.presentation.dto.in.RegisterFile;
import com.junghaebom.geonganghaegym.workout.presentation.dto.out.WorkoutHistoryDto;
import com.junghaebom.geonganghaegym.workout.domain.CompletedExercise;
import com.junghaebom.geonganghaegym.workout.domain.WorkoutHistory;
import com.junghaebom.geonganghaegym.workout.domain.WorkoutHistoryFiles;
import com.junghaebom.geonganghaegym.workout.domain.WorkoutHistoryLike;
import com.junghaebom.geonganghaegym.workout.domain.WorkoutHistoryLikePK;
import com.junghaebom.geonganghaegym.workout.repository.workoutHistory.CompletedExerciseRepository;
import com.junghaebom.geonganghaegym.workout.repository.workoutHistory.WorkoutFileRepository;
import com.junghaebom.geonganghaegym.workout.repository.workoutHistory.WorkoutHistoryLikeRepository;
import com.junghaebom.geonganghaegym.workout.repository.workoutHistory.WorkoutHistoryRepository;
import com.junghaebom.geonganghaegym.file.application.LocalFileStorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WorkoutHistoryService {

	private final FileService fileService;
	private final LocalFileStorageService fileStorageService;
	private final WorkoutHistoryLikeRepository workoutHistoryLikeRepository;
	private final WorkoutHistoryRepository workoutHistoryRepository;
	private final CompletedExerciseRepository completedExerciseRepository;
	private final MemberRepository memberRepository;
	private final MemberDataAccessValidator memberDataAccessValidator;
	private final WorkoutFileRepository workoutFileRepository;

	public WorkoutHistoryDto addWorkoutHistory(Member member, HistoryAddCommand command) {
		Member result = memberRepository.findByIdAndDelYnFalse(member.getId())
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
		WorkoutHistory history = WorkoutHistory.create(command.content(), command.viewMySelf(), member,
			result.getGym());
		workoutHistoryRepository.save(history);
		saveCompletedExercises(history, command);
		uploadNewFiles(history, command.files());
		return WorkoutHistoryDto.from(history);
	}

	private void saveCompletedExercises(WorkoutHistory history, HistoryAddCommand command) {
		List<CompletedExercise> completedExercises = command.completedExercises().stream()
			.map(c -> {
				String name = c.names() != null ? c.names() : c.name();
				return CompletedExercise.create(c.exerciseId(), history, name, c.setNum(), c.weight(),
					c.numberOfCycles());
				}).collect(Collectors.toList());
		completedExerciseRepository.saveAll(completedExercises);
	}

	public CustomPaging getWorkoutHistory(Member loginMember, Long memberId, Pageable pageable, String searchDate) {
		memberDataAccessValidator.validateReadable(loginMember.getId(), memberId);
		Member member = memberRepository.findByIdAndDelYnFalse(memberId)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

		Page<WorkoutHistoryDto> pageDtos = workoutHistoryRepository.getWorkoutHistoryOfMonth(loginMember.getId(),
			memberId, pageable, searchDate);
		List<WorkoutHistoryDto> historiesDto = pageDtos.stream().toList();
		List<Long> ids = historiesDto.stream().map(WorkoutHistoryDto::workoutHistoryId).collect(Collectors.toList());
		historiesDto = setHistoryListFile(historiesDto, ids);
		List<WorkoutHistoryDto> content = setHistoryListExercise(historiesDto, ids);
		CustomPaging customPaging = new CustomPaging<>(content, pageDtos.getPageable().getPageNumber(),
			pageDtos.getPageable().getPageSize(), pageDtos.getTotalPages(), pageDtos.getTotalElements(),
			pageDtos.isLast());
		customPaging.setMainData(MemberDto.from(member));
		return customPaging;
	}

	public WorkoutHistoryDto getWorkoutHistoryDetail(Member loginMember, Long workoutHistoryId) {
		WorkoutHistoryDto historyDto = workoutHistoryRepository.findByWorkoutHistoryId(loginMember.getId(),
			workoutHistoryId);
		List<Long> ids = List.of(workoutHistoryId);
		historyDto = setHistoryFile(historyDto, ids);
		historyDto = setHistoryExercise(historyDto, ids);
		return historyDto;
	}

	public void deleteWorkoutHistory(Member member, Long workoutHistoryId) {
		WorkoutHistory history = workoutHistoryRepository.findByWorkoutHistoryIdAndMemberIdAndDelYnFalse(
				workoutHistoryId, member.getId())
			.orElseThrow(() -> new CustomException(WORKOUT_HISTORY_NOT_FOUND));
		history.deleteWorkoutHistory();
		completedExerciseRepository.deleteAllInBatch(history.getCompletedExercises());
		workoutHistoryLikeRepository.deleteLikeByWorkoutHistoryId(workoutHistoryId);
		history.getHistoryFiles().forEach(file -> fileService.deleteHistoryFile(getFileName(file.getFileUrl())));
	}

	public WorkoutHistoryDto updateWorkoutHistory(Member member, Long workoutHistoryId, HistoryAddCommand command) {
		WorkoutHistory history = workoutHistoryRepository.findByWorkoutHistoryIdAndMemberIdAndDelYnFalse(
				workoutHistoryId, member.getId())
			.orElseThrow(() -> new CustomException(WORKOUT_HISTORY_NOT_FOUND));

		history.changeViewMySelf(command.viewMySelf());
		history.changeContent(command.content());
		updateCompletedExercise(command, history);
		deleteOldFiles(history, command);
		uploadNewFiles(history, command.files());

		WorkoutHistoryDto historyDto = WorkoutHistoryDto.from(history);
		historyDto = setHistoryFile(historyDto, List.of(history.getWorkoutHistoryId()));
		return historyDto;
	}

	private void deleteOldFiles(WorkoutHistory history, HistoryAddCommand command) {
		Set<String> oldFileNames = history.getHistoryFiles().stream()
			.map(WorkoutHistoryFiles::getFileName).collect(Collectors.toSet());
		Set<String> requestFileNames = command.files().stream()
			.map(f -> getFileName(f.fileUrl())).collect(Collectors.toSet());
		oldFileNames.removeAll(requestFileNames);
		Set<String> deleteFileNames = history.getHistoryFiles().stream()
			.map(WorkoutHistoryFiles::getFileName)
			.filter(oldFileNames::contains)
			.collect(Collectors.toSet());

		history.getHistoryFiles().stream()
			.filter(f -> deleteFileNames.contains(f.getFileName()))
			.forEach(f -> {
				fileService.deleteHistoryFile(f.getFileName());
				f.deleteWorkoutHistoryFile();
			});
	}

	private void updateCompletedExercise(HistoryAddCommand command, WorkoutHistory history) {
		completedExerciseRepository.deleteAllInBatch(history.getCompletedExercises());
		completedExerciseRepository.flush();
		saveCompletedExercises(history, command);
	}

	public void likeWorkoutHistory(Member member, Long workoutHistoryId) {
		WorkoutHistory history = workoutHistoryRepository.findByWorkoutHistoryIdAndDelYnFalse(workoutHistoryId)
			.orElseThrow(() -> new CustomException(WORKOUT_HISTORY_NOT_FOUND));
		WorkoutHistoryLikePK likePk = WorkoutHistoryLikePK.create(history, member);
		workoutHistoryLikeRepository.findById(likePk).ifPresent(i -> {
			throw new CustomException(LIKE_ALREADY_EXISTS);
		});
		workoutHistoryLikeRepository.save(WorkoutHistoryLike.from(likePk));
		history.changeLikeCnt(workoutHistoryLikeRepository.getLikeCnt(history.getWorkoutHistoryId()));
	}

	public void deleteLikeWorkoutHistory(Member member, Long workoutHistoryId) {
		WorkoutHistory history = workoutHistoryRepository.findByWorkoutHistoryIdAndDelYnFalse(workoutHistoryId)
			.orElseThrow(() -> new CustomException(WORKOUT_HISTORY_NOT_FOUND));
		workoutHistoryLikeRepository.delete(WorkoutHistoryLike.from(WorkoutHistoryLikePK.create(history, member)));
		history.changeLikeCnt(workoutHistoryLikeRepository.getLikeCnt(history.getWorkoutHistoryId()));
	}

	private WorkoutHistoryDto setHistoryFile(WorkoutHistoryDto historyDto, List<Long> ids) {
		List<WorkoutHistoryFiles> files = workoutHistoryRepository.getWorkoutHistoryFile(ids);
		List<WorkoutHistoryFileDto> filesDto = files.stream()
			.map(WorkoutHistoryFileDto::from)
			.collect(Collectors.toList());
		return historyDto.withFiles(filesDto);
	}

	private List<WorkoutHistoryDto> setHistoryListFile(List<WorkoutHistoryDto> historiesDto, List<Long> ids) {
		List<WorkoutHistoryFiles> files = workoutHistoryRepository.getWorkoutHistoryFile(ids);
		return historiesDto.stream().map(h -> {
			List<WorkoutHistoryFileDto> thisFiles = files.stream().map(WorkoutHistoryFileDto::from)
				.filter(f -> f.workoutHistoryId().equals(h.workoutHistoryId())).collect(Collectors.toList());
			return h.withFiles(thisFiles);
		}).collect(Collectors.toList());
	}

	private WorkoutHistoryDto setHistoryExercise(WorkoutHistoryDto historyDto, List<Long> ids) {
		List<CompletedExercise> exercises = completedExerciseRepository.getCompletedExercise(ids);
		List<CompletedExerciseDto> exerciseDtos = exercises.stream()
			.map(CompletedExerciseDto::from)
			.collect(Collectors.toList());
		return historyDto.withCompletedExercises(exerciseDtos);
	}

	private List<WorkoutHistoryDto> setHistoryListExercise(List<WorkoutHistoryDto> historiesDto, List<Long> ids) {
		List<CompletedExercise> exercises = completedExerciseRepository.getCompletedExercise(ids);
		return historiesDto.stream().map(h -> {
			List<CompletedExerciseDto> exerciseDtos = exercises.stream().map(CompletedExerciseDto::from)
				.filter(e -> e.workoutHistoryId().equals(h.workoutHistoryId())).collect(Collectors.toList());
			return h.withCompletedExercises(exerciseDtos);
		}).collect(Collectors.toList());
	}

	private String getFileName(String url) {
		String[] arr = url.split("/");
		return arr[arr.length - 1];
	}

	private void uploadNewFiles(WorkoutHistory history, List<RegisterFile> files) {
		for (int i = 0; i < files.size(); i++) {
			RegisterFile fileInfo = files.get(i);
			String filePath = fileStorageService.extractFilePath(fileInfo.fileUrl());
			if (filePath.startsWith("temp/")) {
				fileInfo = fileInfo.withFileOrder(i + 1);
				RegisterFile result = fileService.moveDirTempToOrigin("workout-history/", fileInfo.fileUrl());
				workoutFileRepository.save(
					WorkoutHistoryFiles.create(history, result.fileUrl(), fileInfo.fileOrder()));
			}
		}
	}

	public CustomPaging<WorkoutHistoryDto> getWorkoutHistoryOnCommunity(Long memberId, Member loginMember,
		Pageable pageable, String searchDate) {
		if (ObjectUtils.isEmpty(memberId)) { //커뮤니티 목록 조회
			return getCommunityList(loginMember, pageable, searchDate);
		} else { //커뮤니티 학생 한명의 목록 조회
			return getCommunityListByMember(memberId, loginMember, pageable, searchDate);
		}
	}

	private CustomPaging<WorkoutHistoryDto> getCommunityList(Member loginMember, Pageable pageable, String searchDate) {
		Member member = memberRepository.findByIdAndDelYnFalse(loginMember.getId())
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
		Page<WorkoutHistoryDto> pageDtos = workoutHistoryRepository.getWorkoutHistoryOnCommunity(loginMember.getId(),
			member.getGym().getId(), pageable, searchDate);
		List<WorkoutHistoryDto> historiesDto = pageDtos.stream().toList();
		List<Long> ids = historiesDto.stream().map(WorkoutHistoryDto::workoutHistoryId).collect(Collectors.toList());
		historiesDto = setHistoryListFile(historiesDto, ids);
		List<WorkoutHistoryDto> content = setHistoryListExercise(historiesDto, ids);
		return new CustomPaging<>(content, pageDtos.getPageable().getPageNumber(),
			pageDtos.getPageable().getPageSize(), pageDtos.getTotalPages(), pageDtos.getTotalElements(),
			pageDtos.isLast());
	}

	private CustomPaging<WorkoutHistoryDto> getCommunityListByMember(Long memberId, Member loginMember,
		Pageable pageable, String searchDate) {
		Member member = memberRepository.findByIdAndDelYnFalse(memberId)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
		Page<WorkoutHistoryDto> pageDtos = workoutHistoryRepository.getWorkoutHistoryOnCommunityByMember(
			loginMember.getId(), loginMember.getGym().getId(), memberId, pageable, searchDate);
		List<WorkoutHistoryDto> historiesDto = pageDtos.stream().toList();
		List<Long> ids = historiesDto.stream().map(WorkoutHistoryDto::workoutHistoryId).collect(Collectors.toList());
		historiesDto = setHistoryListFile(historiesDto, ids);
		List<WorkoutHistoryDto> content = setHistoryListExercise(historiesDto, ids);

		CustomPaging customPaging = new CustomPaging<>(content, pageDtos.getPageable().getPageNumber(),
			pageDtos.getPageable().getPageSize(), pageDtos.getTotalPages(), pageDtos.getTotalElements(),
			pageDtos.isLast());
		customPaging.setMainData(MemberDto.from(member));
		return customPaging;
	}

}
