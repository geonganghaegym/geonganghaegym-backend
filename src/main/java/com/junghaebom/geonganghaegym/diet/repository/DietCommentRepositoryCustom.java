package com.junghaebom.geonganghaegym.diet.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.junghaebom.geonganghaegym.diet.domain.DietComment;

public interface DietCommentRepositoryCustom {

	Page<DietComment> getCommentsByDietId(Long dietId, Pageable pageable);
}
