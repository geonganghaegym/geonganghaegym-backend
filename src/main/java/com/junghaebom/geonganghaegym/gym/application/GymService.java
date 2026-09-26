package com.junghaebom.geonganghaegym.gym.application;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.junghaebom.geonganghaegym.gym.presentation.dto.out.GymResult;
import com.junghaebom.geonganghaegym.gym.repository.GymRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GymService {

	private final GymRepository gymRepository;

	public List<GymResult> findAllGym() {
		return gymRepository.findAll().stream()
			.map(GymResult::from)
			.collect(Collectors.toList());
	}
}
