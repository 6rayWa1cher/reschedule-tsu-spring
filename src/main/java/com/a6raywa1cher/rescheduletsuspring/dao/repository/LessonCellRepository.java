package com.a6raywa1cher.rescheduletsuspring.dao.repository;

import com.a6raywa1cher.rescheduletsuspring.dao.results.FindGroupsAndSubgroupsResult;
import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface LessonCellRepository extends CrudRepository<LessonCell, String> {
	Set<LessonCell> getAllBy();

	@Query("select lc.faculty from LessonCell as lc group by lc.faculty")
	List<String> getAllFaculties();

	@Query("from LessonCell as lc where lc.group = ?1 and lc.faculty = ?2 order by lc.dayOfWeek, lc.columnPosition")
	List<LessonCell> getAllByGroupAndFaculty(String group, String faculty);

	@Query("select new com.a6raywa1cher.rescheduletsuspring.dao.results.FindGroupsAndSubgroupsResult(" +
			"lc.group, case when sum(lc.subgroup)>0 then lc.countOfSubgroups else 0 end) " +
			"from LessonCell as lc where lc.faculty = ?1 group by lc.group, lc.countOfSubgroups order by lc.group")
	List<FindGroupsAndSubgroupsResult> findGroupsAndSubgroups(String faculty);
}
