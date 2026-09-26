package com.junghaebom.geonganghaegym.trainer.application;

import static com.junghaebom.geonganghaegym.common.error.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.trainer.domain.TrainerMemberMapping;
import com.junghaebom.geonganghaegym.trainer.respository.TrainerMemberMappingRepository;

@ExtendWith(MockitoExtension.class)
class MemberDataAccessValidatorTest {
	@Mock TrainerMemberMappingRepository mappings;
	@InjectMocks MemberDataAccessValidator validator;

	@Test
	@DisplayName("본인 데이터는 매핑 없이 조회할 수 있다")
	void ownerCanRead() {
		assertThatCode(() -> validator.validateReadable(5L, 5L)).doesNotThrowAnyException();
		verifyNoInteractions(mappings);
	}

	@Test
	@DisplayName("매핑된 트레이너는 회원 데이터를 조회할 수 있다")
	void mappedTrainerCanRead() {
		when(mappings.findByTrainerIdAndMemberId(1L, 5L)).thenReturn(Optional.of(mock(TrainerMemberMapping.class)));
		assertThatCode(() -> validator.validateReadable(1L, 5L)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("매핑되지 않은 트레이너나 다른 회원은 조회할 수 없다")
	void othersCannotRead() {
		when(mappings.findByTrainerIdAndMemberId(9L, 5L)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> validator.validateReadable(9L, 5L))
			.isInstanceOf(CustomException.class)
			.extracting(e -> ((CustomException)e).getErrorCode())
			.isEqualTo(MEMBER_NOT_MAPPED);
	}
}
