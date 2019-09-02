package com.a6raywa1cher.rescheduletsuspring.dao.impl;

import com.a6raywa1cher.rescheduletsuspring.dao.interfaces.LessonCellService;
import com.a6raywa1cher.rescheduletsuspring.dao.repository.LessonCellRepository;
import com.a6raywa1cher.rescheduletsuspring.dao.results.FindGroupsAndSubgroupsResult;
import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
public class LessonCellServiceImpl implements LessonCellService {
	private LessonCellRepository repository;

	@Autowired
	public LessonCellServiceImpl(LessonCellRepository repository) {
		this.repository = repository;
	}

	@Override
	public Set<LessonCell> getAll() {
		return repository.getAllBy();
	}

	@Override
	public List<FindGroupsAndSubgroupsResult> findGroupsAndSubgroups(String faculty) {
		return repository.findGroupsAndSubgroups(faculty);
	}

	@Override
	public List<String> getAllFaculties() {
		return repository.getAllFaculties();
	}

	@Override
	public List<LessonCell> getAllByGroup(String group, String faculty) {
		return repository.getAllByGroupAndFaculty(group, faculty);
	}

	@Override
	public Iterable<LessonCell> saveAll(Collection<LessonCell> collection) {
		return repository.saveAll(collection);
	}

	@Override
	public void deleteAll(Collection<LessonCell> collection) {
		repository.deleteAll(collection);
	}
}
