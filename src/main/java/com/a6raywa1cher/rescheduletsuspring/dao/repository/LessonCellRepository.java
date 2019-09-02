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

	@Query("from LessonCell as lc where lc.group = ?1 order by lc.dayOfWeek, lc.columnPosition")
	List<LessonCell> getAllByGroup(String group);

	@Query("select new com.a6raywa1cher.rescheduletsuspring.dao.results.FindGroupsAndSubgroupsResult(" +
			"lc.group, case when sum(lc.subgroup)>0 then lc.countOfSubgroups else 0 end) " +
			"from LessonCell as lc group by lc.group, lc.countOfSubgroups order by lc.group")
	List<FindGroupsAndSubgroupsResult> findGroupsAndSubgroups();
}
