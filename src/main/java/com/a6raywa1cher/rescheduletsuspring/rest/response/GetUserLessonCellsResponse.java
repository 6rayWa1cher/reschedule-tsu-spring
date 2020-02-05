package com.a6raywa1cher.rescheduletsuspring.rest.response;

import com.a6raywa1cher.rescheduletsuspring.rest.mirror.LessonCellMirror;
import lombok.Data;

import java.util.List;

@Data
public class GetUserLessonCellsResponse {
	private List<LessonCellMirror> list;
}
