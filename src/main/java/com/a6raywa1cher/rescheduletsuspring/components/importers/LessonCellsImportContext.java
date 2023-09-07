package com.a6raywa1cher.rescheduletsuspring.components.importers;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LessonCellsImportContext {
	@Builder.Default
	private boolean overrideCache = false;
}
