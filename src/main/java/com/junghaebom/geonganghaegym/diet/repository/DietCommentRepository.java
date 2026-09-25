package com.junghaebom.geonganghaegym.diet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.junghaebom.geonganghaegym.diet.domain.Diet;
import com.junghaebom.geonganghaegym.diet.domain.DietComment;

public interface DietCommentRepository extends JpaRepository<DietComment, Long>, DietCommentRepositoryCustom {

	Long countByDiet(Diet diet);

	Optional<DietComment> findByCommentIdAndDelYnFalse(@Param("commentId") Long parentCommentId);

	Optional<DietComment> findByCommentIdAndMemberIdAndDelYnFalse(Long commentId, Long memberId);

	Long countByDietAndDelYnFalse(Diet diet);

	Long countByDietAndMemberIdAndDelYnFalse(Diet diet, Long trainerId);

}
