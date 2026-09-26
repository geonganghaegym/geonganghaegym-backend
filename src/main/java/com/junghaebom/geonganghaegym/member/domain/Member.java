package com.junghaebom.geonganghaegym.member.domain;

import static com.junghaebom.geonganghaegym.common.error.ErrorCode.COMPLIMENTARY_ACCOUNT_NOT_MODIFIABLE;
import static com.junghaebom.geonganghaegym.member.domain.AlarmStatus.*;
import static com.junghaebom.geonganghaegym.member.domain.MemberType.*;
import static com.junghaebom.geonganghaegym.member.domain.SocialType.NONE;
import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;

import com.junghaebom.geonganghaegym.common.BaseTimeEntity;
import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.gym.domain.Gym;
import com.junghaebom.geonganghaegym.push.domain.MemberToken;
import com.junghaebom.geonganghaegym.schedule.domain.Schedule;
import com.junghaebom.geonganghaegym.schedule.domain.ScheduleWaiting;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = PROTECTED)
@Getter
@DynamicUpdate
@ToString
public class Member extends BaseTimeEntity<Member, Long> {

	// 체험하기 버튼이 로그인하는 공유 계정. 프론트 entity/auth의 체험 계정 ID와 맞춘다.
	private static final Set<String> COMPLIMENTARY_ACCOUNT_USER_IDS = Set.of(
		"healthy-trainer0",
		"healthy-student0"
	);

	@OneToMany(fetch = LAZY, mappedBy = "member")
	@Builder.Default
	@ToString.Exclude
	private final List<MemberToken> memberToken = new ArrayList<>();
	@OneToMany(fetch = LAZY, mappedBy = "trainer")
	@Builder.Default
	@ToString.Exclude
	private final List<Schedule> trainerSchedules = new ArrayList<>();
	@OneToMany(fetch = LAZY, mappedBy = "applicant")
	@Builder.Default
	@ToString.Exclude
	private final List<Schedule> applicantSchedules = new ArrayList<>();
	@OneToMany(fetch = LAZY, mappedBy = "member")
	@Builder.Default
	@ToString.Exclude
	private final List<ScheduleWaiting> scheduleWaitings = new ArrayList<>();
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "member_id")
	private Long id;
	private String userId;
	private String email;
	@ToString.Exclude
	private String password;
	private String name;
	@ManyToOne(fetch = LAZY, cascade = ALL)
	@JoinColumn(name = "member_profile_id")
	@Nullable
	@ToString.Exclude
	private MemberProfile memberProfile;
	@Enumerated(STRING)
	@ColumnDefault("'STUDENT'")
	@Builder.Default
	private MemberType memberType = STUDENT;
	@Enumerated(STRING)
	@ColumnDefault("'ENABLED'")
	@Builder.Default
	private AlarmStatus pushAlarmStatus = ENABLED;
	@Enumerated(STRING)
	@ColumnDefault("'ENABLED'")
	@Builder.Default
	private AlarmStatus communityAlarmStatus = ENABLED;
	@Enumerated(STRING)
	@ColumnDefault("'ENABLED'")
	@Builder.Default
	private AlarmStatus feedbackAlarmStatus = ENABLED;
	@Enumerated(STRING)
	@ColumnDefault("'ENABLED'")
	@Builder.Default
	private AlarmStatus scheduleNoticeStatus = ENABLED;
	@Enumerated(STRING)
	@ColumnDefault("'ENABLED'")
	@Builder.Default
	private AlarmStatus dietNoticeStatus = ENABLED;
	@ManyToOne(fetch = LAZY, cascade = PERSIST)
	@JoinColumn(name = "gym_id")
	@Nullable
	@ToString.Exclude
	private Gym gym;
	@Enumerated(STRING)
	@ColumnDefault("'NONE'")
	@Builder.Default
	private SocialType socialType = NONE;

	private String nickname;

	@Column(length = 512)
	private String socialId;

	@Column(length = 512)
	private String socialRefreshToken;

	@ColumnDefault("false")
	@Builder.Default
	private boolean delYn = false;

	public static Member join(String userId, String email, String name, MemberType memberType, String password) {
		return Member.builder()
			.userId(userId)
			.email(email)
			.password(password)
			.name(name)
			.pushAlarmStatus(ENABLED)
			.memberType(memberType)
			.socialType(NONE)
			.build();
	}

	public static Member join(String email, String name, MemberType memberType, SocialType socialType, String id) {
		return Member.builder()
			.userId(java.util.UUID.randomUUID().toString())
			.email(email)
			.name(name)
			.pushAlarmStatus(ENABLED)
			.memberType(memberType)
			.socialType(socialType)
			.socialId(id)
			.build();
	}

	public static Member join(String email, String name, MemberType memberType, SocialType socialType, String id,
		String socialRefreshToken) {
		return Member.builder()
			.userId(java.util.UUID.randomUUID().toString())
			.email(email)
			.name(name)
			.pushAlarmStatus(ENABLED)
			.memberType(memberType)
			.socialType(socialType)
			.socialId(id)
			.socialRefreshToken(socialRefreshToken)
			.build();
	}

	public static Member join(String userId, String email, String name, MemberType memberType, SocialType socialType,
		String id, String socialRefreshToken) {
		return Member.builder()
			.userId(userId)
			.email(email)
			.name(name)
			.pushAlarmStatus(ENABLED)
			.memberType(memberType)
			.socialType(socialType)
			.socialId(id)
			.socialRefreshToken(socialRefreshToken)
			.build();
	}

	public void updateSocialRefreshToken(String refreshToken) {
		this.socialRefreshToken = refreshToken;
	}

	public void resetPassword(String password) {
		this.password = password;
	}

	public void registerGym(Gym gym) {
		this.gym = gym;
	}

	public boolean isComplimentaryAccount() {
		return COMPLIMENTARY_ACCOUNT_USER_IDS.contains(userId);
	}

	// 여러 사람이 공유하는 체험 계정이 탈퇴되거나 비밀번호가 바뀌면 모두의 체험하기가 막히고,
	// 프로필·이름·이메일이 바뀌면 다음 체험자가 남이 바꾼 정보를 보게 된다.
	public void validateModifiableAccount() {
		if (isComplimentaryAccount()) {
			throw new CustomException(COMPLIMENTARY_ACCOUNT_NOT_MODIFIABLE);
		}
	}

	public void deleteMember() {
		this.delYn = true;
	}

	public void changePassword(String password) {
		this.password = password;
	}

	public void changeName(String name) {
		this.name = name;
	}

	public void changeAlarm(AlarmType type, AlarmStatus alarmStatus) {
		switch (type) {
			case PUSH -> this.pushAlarmStatus = alarmStatus;
			case COMMUNITY -> this.communityAlarmStatus = alarmStatus;
			case FEEDBACK -> this.feedbackAlarmStatus = alarmStatus;
			case SCHEDULENOTICE -> this.scheduleNoticeStatus = alarmStatus;
		}
	}

	public void changeScheduleNotice(AlarmStatus alarmStatus) {
		this.scheduleNoticeStatus = alarmStatus;
	}

	public void changeDietNotice(AlarmStatus alarmStatus) {
		this.dietNoticeStatus = alarmStatus;
	}

	public void assignNickname(String nickname) {
		this.nickname = nickname;
	}

	public void setMemberProfile(MemberProfile memberProfile) {
		this.memberProfile = memberProfile;
	}

	public void changeEmail(String email) {
		this.email = email;
	}

	public void registerProfile(String fileName, String fileUrl) {
		this.memberProfile = MemberProfile.create(fileName, fileUrl, this);
	}

	public String getTransformedMemberType() {
		return switch (this.memberType) {
			case TRAINER -> TRAINER.getDescription();
			case STUDENT -> "회원으";
		};
	}

	public void deleteProfile() {
		this.memberProfile = null;
	}

	public void updateNonMemberInfo(String userId, String email, String name, MemberType memberType, String password) {
		this.userId = userId;
		this.email = email;
		this.password = password;
		this.name = name;
		this.pushAlarmStatus = ENABLED;
		this.memberType = memberType;
		this.socialType = NONE;
	}

}
