package com.a6raywa1cher.rescheduletsuspring.dao.repository;

import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface LessonCellRepository extends CrudRepository<LessonCell, String> {
	Set<LessonCell> getAllBy();
}
