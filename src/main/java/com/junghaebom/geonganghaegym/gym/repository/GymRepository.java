package com.junghaebom.geonganghaegym.gym.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.junghaebom.geonganghaegym.gym.domain.Gym;

public interface GymRepository extends JpaRepository<Gym, Long> {
	Gym findByName(String name);
}
