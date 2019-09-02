package com.a6raywa1cher.rescheduletsuspring.dao.interfaces;

import com.a6raywa1cher.rescheduletsuspring.dao.results.FindGroupsAndSubgroupsResult;
import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface LessonCellService {
	Set<LessonCell> getAll();

	List<FindGroupsAndSubgroupsResult> findGroupsAndSubgroups();

	List<LessonCell> getAllByGroup(String group);

	Iterable<LessonCell> saveAll(Collection<LessonCell> collection);

	void deleteAll(Collection<LessonCell> collection);
}
