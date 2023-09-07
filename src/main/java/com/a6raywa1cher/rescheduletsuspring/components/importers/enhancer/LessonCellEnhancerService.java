package com.a6raywa1cher.rescheduletsuspring.components.importers.enhancer;

import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@AllArgsConstructor
public class LessonCellEnhancerService {
	private final List<LessonCellEnhancerUnit> lessonCellEnhancerUnitList;

	public List<LessonCell> enhance(Collection<LessonCell> lessonCells) {
		List<LessonCell> currentList = new ArrayList<>(lessonCells);
		for (LessonCellEnhancerUnit lessonCellEnhancerUnit : lessonCellEnhancerUnitList) {
			currentList = lessonCellEnhancerUnit.enhance(currentList);
		}
		return currentList;
	}
}
