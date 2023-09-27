package com.a6raywa1cher.rescheduletsuspring.models;

import lombok.*;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "lesson_cell", indexes = {
	@Index(columnList = "faculty,group", name = "Get_for_group")
})
@EqualsAndHashCode(exclude = "creator")
@ToString(exclude = "creator")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonCell {
	@Id
	@Column(columnDefinition = "TEXT")
	private String externalId;

	@Column
	@GeneratedValue
	private Integer internalId;

	@Column
	@Enumerated(EnumType.ORDINAL)
	private WeekSign weekSign;

	@Column(columnDefinition = "TEXT")
	private String fullSubjectName;

	@Column(columnDefinition = "TEXT")
	private String shortSubjectName;

	@Column(columnDefinition = "TEXT")
	private String teacherName;

	@Column(columnDefinition = "TEXT")
	private String teacherTitle;

	@Column
	@Enumerated(EnumType.ORDINAL)
	private DayOfWeek dayOfWeek;

	@Column
	private Integer columnPosition;

	@Column
	private LocalTime start;

	@Column(name = "\"end\"")
	private LocalTime end;

	@Column(columnDefinition = "TEXT")
	private String auditoryAddress;

	@Column
	@Enumerated(EnumType.ORDINAL)
	private Level level;

	@Column
	private Integer course;

	@Column(name = "\"group\"", columnDefinition = "TEXT")
	private String group;

	@Column
	private Integer subgroup;

	@Column
	private Integer countOfSubgroups;

	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean crossPair;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String faculty;

	@ElementCollection(fetch = FetchType.EAGER)
	@Builder.Default
	private List<String> attributes = new ArrayList<>();

	@ManyToOne(fetch = FetchType.LAZY)
	private User creator;

	@Column
	private Boolean ignoreExternalDb;

	@Column
	private String ignoreExternalDbHashCode;
}
