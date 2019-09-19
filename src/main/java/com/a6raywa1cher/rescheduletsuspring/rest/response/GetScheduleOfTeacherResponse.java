package com.a6raywa1cher.rescheduletsuspring.rest.response;

import com.a6raywa1cher.rescheduletsuspring.rest.mirror.LessonCellMirror;
import lombok.Data;

import java.util.List;

@Data
public class GetScheduleOfTeacherResponse {
	private List<LessonCellMirror> rawSchedule;
}
