package com.junghaebom.geonganghaegym.diet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.junghaebom.geonganghaegym.diet.domain.Diet;

public interface DietRepository extends JpaRepository<Diet, Long>, DietRepositoryCustom {
	Optional<Diet> findByDietIdAndDelYnFalse(Long dietId);

	Optional<Diet> findByDietIdAndMemberIdAndDelYnFalse(Long dietId, Long memberId);

}
