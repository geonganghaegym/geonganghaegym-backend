package com.junghaebom.geonganghaegym.diet.application;

import static com.junghaebom.geonganghaegym.common.error.ErrorCode.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.junghaebom.geonganghaegym.common.CustomPaging;
import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.diet.presentation.dto.DietCommentDto;
import com.junghaebom.geonganghaegym.diet.presentation.dto.in.DietCommentAddCommand;
import com.junghaebom.geonganghaegym.diet.domain.Diet;
import com.junghaebom.geonganghaegym.diet.domain.DietComment;
import com.junghaebom.geonganghaegym.diet.repository.DietCommentRepository;
import com.junghaebom.geonganghaegym.diet.repository.DietRepository;
import com.junghaebom.geonganghaegym.member.domain.Member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DietCommentService {

	private final DietCommentRepository commentRepository;
	private final DietRepository dietRepository;

	public CustomPaging<DietCommentDto> getCommentsByDietId(Long dietId, Pageable pageable) {
		Page<DietComment> pageDtos = commentRepository.getCommentsByDietId(dietId, pageable);
		List<DietComment> comments = pageDtos.stream().toList();
		return new CustomPaging<>(settingReplyFormat(comments), pageDtos.getPageable().getPageNumber(),
			pageDtos.getPageable().getPageSize(), pageDtos.getTotalPages(), pageDtos.getTotalElements(),
			pageDtos.isLast());
	}

	private List<DietCommentDto> settingReplyFormat(List<DietComment> comments) {
		List<DietCommentDto> dtos = comments.stream()
			.map(c -> DietCommentDto.create(c, c.getMember().getMemberProfile())).toList();
		Map<Boolean, List<DietCommentDto>> dtos2 = dtos.stream()
			.collect(Collectors.partitioningBy(c -> c.parentId() == null));
		List<DietCommentDto> parent = dtos2.get(true);
		List<DietCommentDto> child = dtos2.get(false);

		Map<Long, List<DietCommentDto>> childByGroupList = child.stream()
			.collect(Collectors.groupingBy(DietCommentDto::parentId, Collectors.toList()));
		return parent.stream().map(p -> p.withReplies(childByGroupList.get(p.id()))).toList();
	}

	public void addComment(Long dietId, DietCommentAddCommand command, Member member) {
		Diet diet = dietRepository.findById(dietId)
			.orElseThrow(() -> new CustomException(DIET_NOT_FOUND));

		boolean isReply = command.parentCommentId() != null;
		Long depth, orderNum;
		Long commentCnt = commentRepository.countByDiet(diet);
		if (isReply) {
			DietComment parentComment = commentRepository.findByCommentIdAndDelYnFalse(command.parentCommentId())
				.orElseThrow(() -> new CustomException(COMMENT_NOT_FOUND));
			depth = parentComment.getDepth() + 1;
			orderNum = parentComment.getOrderNum();
		} else {
			depth = 0L;
			orderNum = commentCnt;
		}
		commentRepository.save(
			DietComment.create(diet, member, command.content(), command.parentCommentId(), depth, orderNum));
		diet.updateCommentCnt(++commentCnt);
	}

	public DietCommentDto updateComment(Member member, Long dietId, Long commentId, DietCommentAddCommand command) {
		DietComment comment = commentRepository.findByCommentIdAndMemberIdAndDelYnFalse(commentId, member.getId())
			.orElseThrow(() -> new CustomException(COMMENT_NOT_FOUND));
		comment.updateContent(command.content());
		return DietCommentDto.from(comment);
	}

	public void deleteComment(Member member, Long dietId, Long commentId) {
		DietComment comment = commentRepository.findByCommentIdAndMemberIdAndDelYnFalse(commentId, member.getId())
			.orElseThrow(() -> new CustomException(COMMENT_NOT_FOUND));
		comment.deleteComment();
	}
}
