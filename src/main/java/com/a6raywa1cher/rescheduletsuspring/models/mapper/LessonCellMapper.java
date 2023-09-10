package com.a6raywa1cher.rescheduletsuspring.models.mapper;

import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import com.a6raywa1cher.rescheduletsuspring.rest.request.CreateLessonCellRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
public interface LessonCellMapper {
	@Mapping(target = "internalId", ignore = true)
	LessonCell map(CreateLessonCellRequest request);

	@Mapping(target = "internalId", ignore = true)
	void copyData(LessonCell from, @MappingTarget LessonCell to);
}
