package com.junghaebom.geonganghaegym.gym.application;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.junghaebom.geonganghaegym.gym.presentation.dto.out.GymResult;
import com.junghaebom.geonganghaegym.gym.presentation.dto.out.TrainersByGymResult;
import com.junghaebom.geonganghaegym.gym.repository.GymRepository;
import com.junghaebom.geonganghaegym.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GymService {

	private final MemberRepository memberRepository;
	private final GymRepository gymRepository;

	public List<GymResult> findAllGym() {
		return gymRepository.findAll().stream()
			.map(GymResult::from)
			.collect(Collectors.toList());
	}

	public List<TrainersByGymResult> findAllTrainersByGym(Long gymId) {
		return memberRepository.findAllTrainerByGym(gymId).stream()
			.map(TrainersByGymResult::from)
			.collect(Collectors.toList());
	}
}
