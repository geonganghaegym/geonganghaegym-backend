package com.junghaebom.geonganghaegym.common.error;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	MEMBER_NOT_FOUND(NOT_FOUND, "C_000", "회원이 존재하지 않습니다."),
	MEMBER_EMAIL_DUPLICATION(BAD_REQUEST, "C_001", "이미 가입된 이메일 주소입니다."),
	ACCESS_TOKEN_EXPIRED(BAD_REQUEST, "C_002", "토큰이 만료되었습니다."),
	HANDLE_ACCESS_DENIED(FORBIDDEN, "C_003", "권한이 없습니다."),
	ACCESS_TOKEN_NOT_FOUND(NOT_FOUND, "C_004", "토큰을 찾을 수 없습니다."),
	REFRESH_TOKEN_EXPIRED(BAD_REQUEST, "C_005", "갱신 토큰이 만료되었습니다."),
	REFRESH_TOKEN_NOT_FOUND(NOT_FOUND, "C_006", "갱신 토큰을 찾을 수 없습니다."),
	NOT_RESERVABLE_SCHEDULE(BAD_REQUEST, "C_007", "신청할 수 없는 일정입니다."),
	APPLICATION_FORM_NOT_FOUND(NOT_FOUND, "C_008", "해당 수업이 존재하지 않습니다."),
	WORKOUT_HISTORY_NOT_FOUND(NOT_FOUND, "C_009", "운동기록이 존재하지 않습니다."),
	DIET_NOT_FOUND(NOT_FOUND, "C_010", "식단기록이 존재하지 않습니다."),
	MAIL_AUTH_CODE_NOT_VALID(BAD_REQUEST, "C_011", "인증번호가 일치하지 않습니다.다시 확인해 주세요."),
	REFRESH_TOKEN_NOT_VALID(BAD_REQUEST, "C_012", "갱신 토큰이 유효하지 않습니다."),
	MEMBER_ID_DUPLICATION(BAD_REQUEST, "C_013", "이미 등록된 아이디입니다."),
	MEMBER_ALREADY_MAPPED(BAD_REQUEST, "C_014", "이미 등록된 회원입니다."),
	MEMBER_NOT_MAPPED(BAD_REQUEST, "C_015", "내 학생이 아닙니다."),
	TRAINER_NOT_MAPPED(BAD_REQUEST, "C_016", "매핑된 트레이너가 없습니다."),
	NOT_SCHEDULE_WAITING(BAD_REQUEST, "C_017", "대기할 수 없는 일정입니다."),
	SCHEDULE_NOT_FOUND(NOT_FOUND, "C_018", "일정을 찾을 수 없습니다."),
	COMMENT_NOT_FOUND(NOT_FOUND, "C_019", "댓글이 존재하지 않습니다."),
	SCHEDULE_WAITING_NOT_FOUND(NOT_FOUND, "C_020", "대기한 일정을 찾을 수 없습니다."),
	NOT_MATCH_PASSWORD(BAD_REQUEST, "C_021", "변경할 비밀번호가 일치하지 않습니다."),
	INVITE_LINK_NOT_FOUND(NOT_FOUND, "C_022", "초대링크를 찾을 수 없습니다."),
	INVITE_NAME_NOT_VALID(BAD_REQUEST, "C_023", "초대회원 이름이 유효하지 않습니다."),
	EXERCISE_NOT_FOUND(NOT_FOUND, "C_024", "해당 운동이 존재하지 않습니다."),
	PASSWORD_NOT_VALID(BAD_REQUEST, "C_025", "비밀번호가 일치하지 않습니다."),
	DATETIME_NOT_VALID(BAD_REQUEST, "C_026", "시작날짜와 종료날짜가 유효하지 않습니다."),
	COURSE_ALREADY_EXISTS(BAD_REQUEST, "C_027", "잔여 수강권이 존재합니다."),
	COURSE_NOT_FOUND(NOT_FOUND, "C_028", "수강권이 존재하지 않습니다."),
	COURSE_IS_USING(BAD_REQUEST, "C_029", "진행중이거나 만료된 수강권은 삭제할 수 없습니다."),
	LESSON_CNT_NOT_VALID(BAD_REQUEST, "C_030", "수강권 횟수가 유효하지 않습니다."),
	LIKE_ALREADY_EXISTS(BAD_REQUEST, "C_031", "이미 좋아요 하였습니다."),
	DATE_NOT_VALID(BAD_REQUEST, "C_032", "날짜가 유효하지 않습니다."),
	DIET_ALREADY_EXISTS(BAD_REQUEST, "C_033", "해당 날짜에 식단기록이 존재합니다."),
	DIET_NOT_VALID(BAD_REQUEST, "C_034", "사진 및 단식을 등록해주세요."),
	MEMBER_LOGIN_FAILED(BAD_REQUEST, "C_035", "로그인에 실패했어요."),
	EXERCISE_ALREADY_EXISTS(BAD_REQUEST, "C_036", "이미 등록된 운동이 있습니다."),
	RESERVATION_ALREADY_EXISTS(BAD_REQUEST, "C_037", "예약된 수업이 있어 수강권을 삭제할 수 없습니다."),
	LESSON_CNT_MAX(BAD_REQUEST, "C_038", "수강권 횟수는 500회를 초과할 수 없습니다."),
	COURSE_ONLY_PLUS(BAD_REQUEST, "C_039", "횟수는 추가만 가능합니다."),
	COURSE_POSITIVE(BAD_REQUEST, "C_040", "양수를 입력해주세요."),

	MEMBER_NAME_LENGTH_NOT_VALID(BAD_REQUEST, "C_050", "이름은 최소 2자 이상 입력해 주세요."),
	MEMBER_NAME_NOT_VALID(BAD_REQUEST, "C_051", "이름은 한글 또는 영어만 입력할 수 있습니다."),
	CONFIRM_PASSWORD_NOT_MATCHED(BAD_REQUEST, "C_052", "비밀번호가 일치하지 않습니다."),
	USERID_POLICY_VIOLATION(BAD_REQUEST, "C_053", "아이디에 한글을 포함할 수 없습니다."),
	PASSWORD_POLICY_VIOLATION(BAD_REQUEST, "C_054", "영문+숫자 조합 8자리 이상을 입력해 주세요."),
	GYM_NOT_FOUND(NOT_FOUND, "C_055", "등록된 헬스장이 없습니다."),
	GYM_DUPLICATION(BAD_REQUEST, "C_056", "중복된 헬스장이 존재합니다."),
	JOIN_CODE_NOT_VALID(BAD_REQUEST, "C_057", "인증코드가 일치하지 않습니다. 다시 확인해 주세요."),
	MEMBER_ID_NOT_VALID(BAD_REQUEST, "C_058", "영문 4자리 이상 입력해 주세요."),
	TRAINER_NOT_FOUND(NOT_FOUND, "C_059", "트레이너가 존재하지 않습니다."),
	LESSON_HISTORY_NOT_FOUND(NOT_FOUND, "C_060", "수업 일지가 존재하지 않습니다."),
	LESSON_HISTORY_COMMENT_NOT_FOUND(NOT_FOUND, "C_061", "수업 일지 댓글이 존재하지 않습니다."),
	EXCEED_MAXIMUM_NUMBER_OF_FILES(BAD_REQUEST, "C_062", "파일 업로드는 최대 3개까지 가능합니다."),
	LUNCH_TIME_INVALID(BAD_REQUEST, "C_063", "시작 점심시간이 종료 점심시간과 같거나 빠를 수 없습니다."),
	SCHEDULE_LESS_THAN_31_DAYS(BAD_REQUEST, "C_064", "일정 등록은 31일을 넘을 수 없습니다."),
	START_DATE_AFTER_END_DATE(BAD_REQUEST, "C_065", "수업 시작일은 종료일보다 빨라야 합니다."),
	SCHEDULE_ALREADY_EXISTS(BAD_REQUEST, "C_066", "이미 등록된 일정이 존재합니다."),
	RESERVATION_NOT_VALID(BAD_REQUEST, "C_067", "예약 가능한 시간이 아닙니다."),
	RESERVATION_CANCEL_NOT_VALID(BAD_REQUEST, "C_068", "취소 가능한 시간이 아닙니다."),
	UNCHANGED_GYM_ID(BAD_REQUEST, "C_069", "변경하려는 헬스장은 이전과 동일한 헬스장입니다."),
	TRAINER_SCHEDULE_NOT_FOUND(BAD_REQUEST, "C_070", "미리 스케줄을 등록 후 사용해 주세요."),
	INVALID_LESSON_TIME_DESCRIPTION(BAD_REQUEST, "C_071", "유효하지 않은 수업 시간입니다."),
	SEARCH_LESS_THAN_31_DAYS(BAD_REQUEST, "C_072", "일정 조회는 31일을 넘을 수 없습니다."),
	START_TIME_AFTER_END_TIME(BAD_REQUEST, "C_073", "수업 시작 시간은 종료 시간보다 빨라야 합니다."),
	RESERVATION_STATUS_NOT_FOUND(BAD_REQUEST, "C_074", "올바른 예약 상태를 입력해 주세요."),
	DEFAULT_LESSONTIME_NOT_VALID(BAD_REQUEST, "C_075", "근무 시간은 오전 6시부터 밤 12시까지 설정이 가능해요."),
	SOCIAL_EMAIL_NOT_PROVIDED(BAD_REQUEST, "C_076", "이메일 제공에 동의해야 소셜 로그인을 완료할 수 있어요."),
	APPLE_ID_TOKEN_NOT_VALID(BAD_REQUEST, "C_077", "애플 로그인 정보가 유효하지 않습니다. 다시 시도해 주세요."),
	COMPLIMENTARY_ACCOUNT_NOT_MODIFIABLE(FORBIDDEN, "C_078", "체험 계정은 탈퇴하거나 계정 정보를 바꿀 수 없어요."),

	SERVER_ERROR(INTERNAL_SERVER_ERROR, "S_001", "서버에서 오류가 발생하였습니다."),
	FILE_UPLOAD_ERROR(INTERNAL_SERVER_ERROR, "S_002", "파일 업로드중 에러가 발생하였습니다."),
	FILE_FIND_ERROR(INTERNAL_SERVER_ERROR, "S_003", "파일 조회중 에러가 발생하였습니다."),
	MAIL_SEND_ERROR(INTERNAL_SERVER_ERROR, "S_004", "메일 전송중 에러가 발생했습니다."),
	FILE_REMOVE_ERROR(INTERNAL_SERVER_ERROR, "S_005", "파일 삭제중 에러가 발생하였습니다."),
	NAVER_CONNECTION_ERROR(INTERNAL_SERVER_ERROR, "S_006", "네이버 소셜 서버와 연동중 에러가 발생하였습니다."),
	KAKAO_CONNECTION_ERROR(INTERNAL_SERVER_ERROR, "S_007", "카카오 소셜 서버와 연동중 에러가 발생하였습니다."),
	JSON_PARSING_ERROR(INTERNAL_SERVER_ERROR, "S_008", "JSON 토큰을 파싱중 에러가 발생하였습니다."),
	PROFILE_ACCESS_FAILED(INTERNAL_SERVER_ERROR, "S_009", "소셜 프로필을 가져오던 중 에러가 발생하였습니다."),
	GOOGLE_CONNECTION_ERROR(INTERNAL_SERVER_ERROR, "S_010", "구글 소셜 서버와 연동중 에러가 발생하였습니다."),
	APPLE_CONNECTION_ERROR(INTERNAL_SERVER_ERROR, "S_011", "애플 소셜 서버와 연동중 에러가 발생하였습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
