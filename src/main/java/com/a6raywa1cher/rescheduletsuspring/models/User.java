package com.a6raywa1cher.rescheduletsuspring.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(exclude = "lessonCellList")
@ToString(exclude = "lessonCellList")
public class User {
	@Id
	@GeneratedValue
	private Long id;

	@Column(unique = true)
	private String username;

	@JsonIgnore
	@Column(length = 256)
	private String password;

	@Column
	private boolean admin;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "creator")
	private List<LessonCell> lessonCellList;

	@ElementCollection
	private List<String> permissions;
}
