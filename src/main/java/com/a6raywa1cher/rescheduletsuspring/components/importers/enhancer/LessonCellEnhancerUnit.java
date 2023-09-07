package com.a6raywa1cher.rescheduletsuspring.components.importers.enhancer;

import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;

import java.util.List;

public interface LessonCellEnhancerUnit {
	List<LessonCell> enhance(List<LessonCell> lessonCells);
}
