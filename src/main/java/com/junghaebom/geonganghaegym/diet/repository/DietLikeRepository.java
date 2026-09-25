package com.junghaebom.geonganghaegym.diet.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.junghaebom.geonganghaegym.diet.domain.DietLike;
import com.junghaebom.geonganghaegym.diet.domain.DietLikePK;

public interface DietLikeRepository extends JpaRepository<DietLike, DietLikePK>, DietLikeRepositoryCustom {

}
