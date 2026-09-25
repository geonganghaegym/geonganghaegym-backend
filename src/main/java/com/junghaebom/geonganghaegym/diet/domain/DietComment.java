package com.junghaebom.geonganghaegym.diet.domain;

import org.hibernate.annotations.ColumnDefault;

import com.junghaebom.geonganghaegym.common.BaseTimeEntity;
import com.junghaebom.geonganghaegym.member.domain.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "diet_comment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@ToString
public class DietComment extends BaseTimeEntity<DietComment, Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "comment_id")
	private Long commentId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "diet_id")
	@ToString.Exclude
	private Diet diet;

	@ManyToOne(fetch = FetchType.LAZY)
	@ToString.Exclude
	@JoinColumn(name = "member_id")
	private Member member;

	private String content;

	@ColumnDefault("false")
	@Builder.Default
	private Boolean delYn = false;

	private Long parentCommentId;
	private Long depth;
	private Long orderNum;

	public static DietComment create(Diet diet,
		Member member,
		String content,
		Long parentCommentId,
		Long depth,
		Long orderNum) {
		return DietComment.builder()
			.diet(diet)
			.member(member)
			.content(content)
			.parentCommentId(parentCommentId)
			.depth(depth)
			.orderNum(orderNum)
			.build();
	}

	public void deleteComment() {
		this.delYn = true;
	}

	public void updateContent(String content) {
		this.content = content;
	}
}
