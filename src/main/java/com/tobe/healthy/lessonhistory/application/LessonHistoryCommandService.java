package com.tobe.healthy.lessonhistory.application;

import static com.tobe.healthy.common.FileUpload.*;
import static com.tobe.healthy.common.FileUpload.FILE_TEMP_UPLOAD_TIMEOUT;
import static com.tobe.healthy.common.Utils.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tobe.healthy.common.error.CustomException;
import com.tobe.healthy.common.error.ErrorCode;
import com.tobe.healthy.common.event.CustomEventPublisher;
import com.tobe.healthy.common.event.EventType;
import com.tobe.healthy.common.redis.RedisKeyPrefix;
import com.tobe.healthy.common.redis.RedisService;
import com.tobe.healthy.config.security.CustomMemberDetails;
import com.tobe.healthy.file.application.LocalFileStorageService;
import com.tobe.healthy.lessonhistory.presentation.dto.in.CommandRegisterComment;
import com.tobe.healthy.lessonhistory.presentation.dto.in.CommandRegisterLessonHistory;
import com.tobe.healthy.lessonhistory.presentation.dto.in.CommandUpdateComment;
import com.tobe.healthy.lessonhistory.presentation.dto.in.CommandUpdateLessonHistory;
import com.tobe.healthy.lessonhistory.presentation.dto.out.CommandRegisterCommentResult;
import com.tobe.healthy.lessonhistory.presentation.dto.out.CommandRegisterLessonHistoryResult;
import com.tobe.healthy.lessonhistory.presentation.dto.out.CommandRegisterReplyResult;
import com.tobe.healthy.lessonhistory.presentation.dto.out.CommandUpdateCommentResult;
import com.tobe.healthy.lessonhistory.presentation.dto.out.CommandUpdateLessonHistoryResult;
import com.tobe.healthy.lessonhistory.presentation.dto.out.CommandUploadFileResult;
import com.tobe.healthy.lessonhistory.domain.LessonHistory;
import com.tobe.healthy.lessonhistory.domain.LessonHistoryComment;
import com.tobe.healthy.lessonhistory.domain.LessonHistoryFiles;
import com.tobe.healthy.lessonhistory.repository.LessonHistoryCommentRepository;
import com.tobe.healthy.lessonhistory.repository.LessonHistoryFilesRepository;
import com.tobe.healthy.lessonhistory.repository.LessonHistoryRepository;
import com.tobe.healthy.member.domain.Member;
import com.tobe.healthy.member.domain.MemberType;
import com.tobe.healthy.member.repository.MemberRepository;
import com.tobe.healthy.notification.presentation.dto.in.CommandSendNotification;
import com.tobe.healthy.notification.domain.NotificationCategory;
import com.tobe.healthy.notification.domain.NotificationType;
import com.tobe.healthy.schedule.domain.Schedule;
import com.tobe.healthy.schedule.repository.TrainerScheduleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LessonHistoryCommandService {

	private final LessonHistoryRepository lessonHistoryRepository;
	private final LessonHistoryFilesRepository lessonHistoryFilesRepository;
	private final MemberRepository memberRepository;
	private final TrainerScheduleRepository trainerScheduleRepository;
	private final LessonHistoryCommentRepository lessonHistoryCommentRepository;
	private final LocalFileStorageService fileStorageService;
	private final RedisService redisService;
	private final CustomEventPublisher<CommandSendNotification> notificationPublisher;

	public CommandRegisterLessonHistoryResult registerLessonHistory(CommandRegisterLessonHistory request,
		Long trainerId) {
		Member student = findMember(request.studentId());
		Member trainer = findMember(trainerId);
		Schedule schedule = findSchedule(request.scheduleId());

		if (LocalDateTime.now().isBefore(LocalDateTime.of(schedule.getLessonDt(), schedule.getLessonEndTime()))) {
			throw new IllegalArgumentException("수업이 끝나기 전에 수업일지를 작성할 수 없습니다.");
		}

		if (lessonHistoryRepository.validateDuplicateLessonHistory(trainerId, student.getId(), schedule.getId())) {
			throw new IllegalArgumentException("이미 수업일지를 등록하였습니다.");
		}

		LessonHistory lessonHistory = LessonHistory.register(request.title(), request.content(), student, trainer,
			schedule);
		lessonHistoryRepository.save(lessonHistory);

		List<LessonHistoryFiles> files = registerFiles(request.uploadFiles(), trainer, lessonHistory);

		sendNotification(
			NotificationType.WRITE,
			NotificationType.WRITE.getContent(),
			lessonHistory.getId(),
			lessonHistory.getStudent().getId(),
			WEB_BASE_URL + "/student/log/" + lessonHistory.getId(),
			null,
			null
		);

		return CommandRegisterLessonHistoryResult.from(lessonHistory, files);
	}

	private void sendNotification(NotificationType notificationType, String content, Long lessonHistoryId,
		Long memberId, String clickUrl, Long studentId, String studentName) {
		CommandSendNotification notification = new CommandSendNotification(
			notificationType.getDescription(),
			content,
			List.of(memberId),
			notificationType,
			NotificationCategory.SCHEDULE,
			lessonHistoryId,
			clickUrl,
			studentId,
			studentName
		);

		notificationPublisher.publish(notification, EventType.NOTIFICATION);
	}

	public List<CommandUploadFileResult> registerFilesOfLessonHistory(List<MultipartFile> uploadFiles, Long memberId) {
		List<CommandUploadFileResult> commandUploadFileResult = new ArrayList<>();

		int fileOrder = 1;

		checkMaximumFileCount(uploadFiles.size());

		for (MultipartFile uploadFile : uploadFiles) {
			if (!uploadFile.isEmpty()) {
				String fileUrl = putFile(uploadFile);
				commandUploadFileResult.add(new CommandUploadFileResult(fileUrl, fileOrder++));
				redisService.setValuesWithTimeout(
					RedisKeyPrefix.TEMP_FILE_URI.getDescription() + fileUrl,
					String.valueOf(memberId),
					(long)FILE_TEMP_UPLOAD_TIMEOUT.getDescription()
				);
			}
		}

		return commandUploadFileResult;
	}

	public CommandUpdateLessonHistoryResult updateLessonHistory(Long lessonHistoryId,
		CommandUpdateLessonHistory request, Long trainerId) {
		LessonHistory lessonHistory = lessonHistoryRepository.findOneLessonHistoryWithFiles(lessonHistoryId, trainerId);
		if (lessonHistory == null) {
			throw new CustomException(ErrorCode.LESSON_HISTORY_NOT_FOUND);
		}

		lessonHistory.updateLessonHistory(request.title(), request.content());

		lessonHistory.getFiles().clear();

		List<LessonHistoryFiles> savedFiles = new ArrayList<>();

		if (!request.uploadFiles().isEmpty()) {
			List<CommandUploadFileResult> requestFiles = request.uploadFiles();
			for (int idx = 0; idx < requestFiles.size(); idx++) {
				CommandUploadFileResult file = requestFiles.get(idx);
				int existingIdx = findFileIndex(lessonHistory.getFiles(), file.fileUrl());
				if (existingIdx != -1) {
					lessonHistory.getFiles().get(existingIdx).updateFileOrder(idx + 1);
					savedFiles.add(lessonHistory.getFiles().get(existingIdx));
				} else {
					if (isTempFile(file.fileUrl())) {
						String tempPath = fileStorageService.extractFilePath(file.fileUrl());
						CommandUploadFileResult result = moveDirTempToOrigin("origin/lesson-history/", tempPath,
							idx + 1);
						LessonHistoryFiles newFile = new LessonHistoryFiles(result.fileUrl(), idx + 1,
							lessonHistory.getTrainer(), lessonHistory);
						savedFiles.add(newFile);
					} else {
						LessonHistoryFiles newFile = new LessonHistoryFiles(file.fileUrl(), idx + 1,
							lessonHistory.getTrainer(), lessonHistory);
						savedFiles.add(newFile);
					}
				}
			}
			lessonHistoryFilesRepository.deleteAll(lessonHistory.getFiles());
			lessonHistory.getFiles().clear();
			lessonHistory.getFiles().addAll(savedFiles);
		} else {
			lessonHistoryFilesRepository.deleteAll(lessonHistory.getFiles());
			lessonHistory.getFiles().clear();
		}

		return CommandUpdateLessonHistoryResult.from(lessonHistory, savedFiles);
	}

	public Long deleteLessonHistory(Long lessonHistoryId, Long trainerId) {
		LessonHistory lessonHistory = lessonHistoryRepository.findByIdAndTrainerId(lessonHistoryId, trainerId);
		if (lessonHistory == null) {
			throw new CustomException(ErrorCode.LESSON_HISTORY_NOT_FOUND);
		}

		deleteAllFiles(lessonHistory.getFiles());

		lessonHistoryRepository.deleteById(lessonHistory.getId());

		return lessonHistoryId;
	}

	public CommandRegisterCommentResult registerLessonHistoryComment(Long lessonHistoryId,
		CommandRegisterComment request, CustomMemberDetails member) {
		Member findMember = findMember(member.getMemberId());

		LessonHistory lessonHistory = lessonHistoryRepository.findById(lessonHistoryId, member.getMemberId(),
			member.getMemberType());
		if (lessonHistory == null) {
			throw new CustomException(ErrorCode.LESSON_HISTORY_NOT_FOUND);
		}

		int order = lessonHistoryCommentRepository.findTopComment(lessonHistory.getId(), null);
		LessonHistoryComment lessonHistoryComment = registerComment(order, request, findMember, lessonHistory);
		List<LessonHistoryFiles> files = registerFile(request.uploadFiles(), findMember, lessonHistory,
			lessonHistoryComment);

		if (!member.getMemberId().equals(lessonHistory.getTrainer().getId())) {
			sendNotification(
				NotificationType.COMMENT,
				NotificationType.COMMENT.getContent(),
				lessonHistory.getId(),
				lessonHistory.getTrainer().getId(),
				WEB_BASE_URL + "/trainer/manage/" + lessonHistory.getStudent().getId() + "/log/"
					+ lessonHistory.getId(),
				lessonHistory.getStudent().getId(),
				lessonHistory.getStudent().getName()
			);
		}

		return CommandRegisterCommentResult.from(lessonHistoryComment, files);
	}

	public CommandRegisterReplyResult registerLessonHistoryReply(Long lessonHistoryId, Long lessonHistoryCommentId,
		CommandRegisterComment request, CustomMemberDetails member) {
		Member findMember = findMember(member.getMemberId());

		LessonHistory lessonHistory = lessonHistoryRepository.findById(lessonHistoryId, member.getMemberId(),
			member.getMemberType());
		if (lessonHistory == null) {
			throw new CustomException(ErrorCode.LESSON_HISTORY_NOT_FOUND);
		}

		int order = lessonHistoryCommentRepository.findTopComment(lessonHistory.getId(), lessonHistoryCommentId);

		LessonHistoryComment parentComment = lessonHistoryCommentRepository.findById(lessonHistoryCommentId)
			.orElseThrow(() -> new CustomException(ErrorCode.LESSON_HISTORY_COMMENT_NOT_FOUND));

		LessonHistoryComment entity = new LessonHistoryComment(order, request.content(), findMember, lessonHistory,
			parentComment);

		if (!parentComment.getWriter().getId().equals(member.getMemberId())) {
			if (parentComment.getWriter().getMemberType() == MemberType.STUDENT) {
				sendNotification(
					NotificationType.REPLY,
					NotificationType.REPLY.getContent(),
					lessonHistory.getId(),
					parentComment.getWriter().getId(),
					WEB_BASE_URL + "/student/log/" + lessonHistory.getId(),
					lessonHistory.getStudent().getId(),
					lessonHistory.getStudent().getName()
				);
			} else if (parentComment.getWriter().getMemberType() == MemberType.TRAINER) {
				sendNotification(
					NotificationType.REPLY,
					NotificationType.REPLY.getContent(),
					lessonHistory.getId(),
					parentComment.getWriter().getId(),
					WEB_BASE_URL + "/trainer/manage/" + lessonHistory.getStudent().getId() + "/log/"
						+ lessonHistory.getId(),
					lessonHistory.getStudent().getId(),
					lessonHistory.getStudent().getName()
				);
			}
		}

		lessonHistoryCommentRepository.save(entity);

		List<LessonHistoryFiles> files = registerFile(request.uploadFiles(), findMember, lessonHistory, entity);

		return CommandRegisterReplyResult.from(entity, files);
	}

	public CommandUpdateCommentResult updateLessonHistoryComment(Long lessonHistoryCommentId,
		CommandUpdateComment request, CustomMemberDetails member) {
		LessonHistoryComment comment = lessonHistoryCommentRepository.findLessonHistoryCommentWithFiles(
			lessonHistoryCommentId, member.getMemberId());
		if (comment == null) {
			throw new CustomException(ErrorCode.LESSON_HISTORY_COMMENT_NOT_FOUND);
		}

		List<LessonHistoryFiles> savedFiles = new ArrayList<>();

		if (!request.uploadFiles().isEmpty()) {
			List<CommandUploadFileResult> requestFiles = request.uploadFiles();
			for (int idx = 0; idx < requestFiles.size(); idx++) {
				CommandUploadFileResult file = requestFiles.get(idx);
				int existingIdx = findFileIndex(comment.getFiles(), file.fileUrl());
				if (existingIdx != -1) {
					comment.getFiles().get(existingIdx).updateFileOrder(idx + 1);
					savedFiles.add(comment.getFiles().get(existingIdx));
				} else {
					if (isTempFile(file.fileUrl())) {
						String tempPath = fileStorageService.extractFilePath(file.fileUrl());
						CommandUploadFileResult result = moveDirTempToOrigin("origin/lesson-history/", tempPath,
							idx + 1);
						LessonHistoryFiles newFile = new LessonHistoryFiles(result.fileUrl(), idx + 1,
							comment.getWriter(), comment.getLessonHistory(), comment);
						savedFiles.add(newFile);
					} else {
						LessonHistoryFiles newFile = new LessonHistoryFiles(file.fileUrl(), idx + 1,
							comment.getWriter(), comment.getLessonHistory(), comment);
						savedFiles.add(newFile);
					}
				}
			}
			lessonHistoryFilesRepository.deleteAll(comment.getFiles());
			comment.getFiles().clear();
			comment.getFiles().addAll(savedFiles);
		} else {
			lessonHistoryFilesRepository.deleteAll(comment.getFiles());
			comment.getFiles().clear();
		}

		comment.updateLessonHistoryComment(request.content());

		return CommandUpdateCommentResult.from(comment);
	}

	public Long deleteLessonHistoryComment(Long lessonHistoryCommentId, Long writerId) {
		LessonHistoryComment comment = lessonHistoryCommentRepository.findById(lessonHistoryCommentId, writerId);
		if (comment == null) {
			throw new CustomException(ErrorCode.LESSON_HISTORY_COMMENT_NOT_FOUND);
		}

		deleteAllFiles(comment.getFiles());

		comment.deleteComment();

		return lessonHistoryCommentId;
	}

	private boolean isTempFile(String fileUrl) {
		String filePath = fileStorageService.extractFilePath(fileUrl);
		return filePath.startsWith("temp/");
	}

	private List<LessonHistoryFiles> registerFile(List<CommandUploadFileResult> uploadFiles, Member member,
		LessonHistory lessonHistory, LessonHistoryComment lessonHistoryComment) {
		checkMaximumFileCount(uploadFiles.size());

		List<LessonHistoryFiles> files = new ArrayList<>();

		for (int idx = 0; idx < uploadFiles.size(); idx++) {
			CommandUploadFileResult uploadFile = uploadFiles.get(idx);
			if (isTempFile(uploadFile.fileUrl())) {
				String tempPath = fileStorageService.extractFilePath(uploadFile.fileUrl());
				CommandUploadFileResult result = moveDirTempToOrigin("origin/lesson-history/", tempPath, idx + 1);
				LessonHistoryFiles file = new LessonHistoryFiles(result.fileUrl(), result.fileOrder(), member,
					lessonHistory, lessonHistoryComment);
				files.add(file);
			}
		}

		lessonHistoryFilesRepository.saveAll(files);

		return files;
	}

	private LessonHistoryComment registerComment(int order, CommandRegisterComment request, Member findMember,
		LessonHistory lessonHistory) {
		LessonHistoryComment entity = new LessonHistoryComment(order, request.content(), findMember, lessonHistory);
		lessonHistoryCommentRepository.save(entity);
		return entity;
	}

	private String putFile(MultipartFile uploadFile) {
		String originalFilename = uploadFile.getOriginalFilename();
		String savedFileName = createFileName(
			"origin/lesson-history/",
			originalFilename.substring(originalFilename.lastIndexOf("."))
		);
		try {
			String fileUrl = fileStorageService.store(savedFileName, uploadFile.getInputStream());
			log.info("등록된 파일 URL => {}", fileUrl);
			return fileUrl;
		} catch (Exception e) {
			throw new RuntimeException("파일 업로드에 실패했습니다.", e);
		}
	}

	private Schedule findSchedule(Long scheduleId) {
		return trainerScheduleRepository.findById(scheduleId)
			.orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));
	}

	private List<LessonHistoryFiles> registerFiles(List<CommandUploadFileResult> uploadFiles, Member member,
		LessonHistory lessonHistory) {
		checkMaximumFileCount(uploadFiles.size());

		List<LessonHistoryFiles> files = new ArrayList<>();

		for (int idx = 0; idx < uploadFiles.size(); idx++) {
			CommandUploadFileResult uploadFile = uploadFiles.get(idx);
			if (isTempFile(uploadFile.fileUrl())) {
				String tempPath = fileStorageService.extractFilePath(uploadFile.fileUrl());
				CommandUploadFileResult result = moveDirTempToOrigin("origin/lesson-history/", tempPath, idx + 1);
				LessonHistoryFiles file = new LessonHistoryFiles(result.fileUrl(), result.fileOrder(), member,
					lessonHistory);
				files.add(file);
			}
		}

		lessonHistoryFilesRepository.saveAll(files);

		return files;
	}

	public CommandUploadFileResult moveDirTempToOrigin(String originDir, String tempPath, int idx) {
		String originPath = originDir + tempPath.replaceFirst("temp/", "");
		fileStorageService.copy(tempPath, originPath);
		String fileUrl = fileStorageService.getFileUrl(originPath);
		log.info("등록한 fileUrl: {}", fileUrl);
		return new CommandUploadFileResult(fileUrl, idx);
	}

	private void checkMaximumFileCount(int uploadFilesSize) {
		if (uploadFilesSize > FILE_MAXIMUM_UPLOAD_SIZE.getDescription()) {
			throw new CustomException(ErrorCode.EXCEED_MAXIMUM_NUMBER_OF_FILES);
		}
	}

	private Member findMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
	}

	private void deleteAllFiles(List<LessonHistoryFiles> files) {
		for (LessonHistoryFiles file : files) {
			String filePath = fileStorageService.extractFilePath(file.getFileUrl());
			fileStorageService.delete(filePath);
		}
		lessonHistoryFilesRepository.deleteAll(files);
	}

	private int findFileIndex(List<LessonHistoryFiles> files, String fileUrl) {
		for (int i = 0; i < files.size(); i++) {
			if (files.get(i).getFileUrl().equals(fileUrl)) {
				return i;
			}
		}
		return -1;
	}
}
