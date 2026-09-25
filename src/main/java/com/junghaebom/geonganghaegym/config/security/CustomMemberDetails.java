package com.junghaebom.geonganghaegym.config.security;

import static com.junghaebom.geonganghaegym.member.domain.MemberType.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.domain.MemberType;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class CustomMemberDetails implements UserDetails {

	private Member member;

	public CustomMemberDetails(Member member) {
		this.member = member;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> roles = new ArrayList<>();
		if (member.getMemberType().equals(STUDENT)) {
			roles.add(new SimpleGrantedAuthority("ROLE_STUDENT"));
		} else if (member.getMemberType().equals(TRAINER)) {
			roles.add(new SimpleGrantedAuthority("ROLE_TRAINER"));
		}
		return roles;
	}

	@Override
	public String getPassword() {
		return member.getPassword();
	}

	@Override
	public String getUsername() {
		return member.getUserId();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	public Long getMemberId() {
		return member.getId();
	}

	public MemberType getMemberType() {
		return member.getMemberType();
	}
}
