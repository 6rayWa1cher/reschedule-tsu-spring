package com.a6raywa1cher.rescheduletsuspring.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
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
}
