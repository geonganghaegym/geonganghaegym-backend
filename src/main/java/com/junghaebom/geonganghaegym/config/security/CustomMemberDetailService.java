package com.junghaebom.geonganghaegym.config.security;

import static com.junghaebom.geonganghaegym.common.error.ErrorCode.*;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomMemberDetailService implements UserDetailsService {

	private final MemberRepository memberRepository;

	@Override
	public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
		return memberRepository.findById(Long.valueOf(userId))
			.map(CustomMemberDetails::new)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
	}
}
