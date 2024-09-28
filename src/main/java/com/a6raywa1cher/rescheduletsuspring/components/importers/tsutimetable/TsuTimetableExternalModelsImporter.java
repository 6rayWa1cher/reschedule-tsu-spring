package com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable;

import com.a6raywa1cher.rescheduletsuspring.components.importers.AbstractExternalModelsImporter;
import com.a6raywa1cher.rescheduletsuspring.components.importers.ImportException;
import com.a6raywa1cher.rescheduletsuspring.components.importers.LessonCellSynchronizationService;
import com.a6raywa1cher.rescheduletsuspring.components.importers.enhancer.LessonCellEnhancerService;
import com.a6raywa1cher.rescheduletsuspring.components.importers.loader.DataLoader;
import com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable.models.group.GroupScheduleDto;
import com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable.models.group.LessonDto;
import com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable.models.group.LessonTimeDto;
import com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable.models.selectors.SelectorGroupDto;
import com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable.models.selectors.SelectorHolderDto;
import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import com.a6raywa1cher.rescheduletsuspring.models.submodels.LessonCellCoordinates;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.*;

@Service
@ConditionalOnExpression("${app.tsudb.enabled:true} && '${app.tsudb.remote-type:timetable}' == 'timetable'")
@AllArgsConstructor
@Slf4j
public class TsuTimetableExternalModelsImporter extends AbstractExternalModelsImporter {
	private final ObjectMapper objectMapper = new ObjectMapper()
		.registerModule(new Jdk8Module())
		.registerModule(new JavaTimeModule())
		.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	private final LessonCellSynchronizationService lessonCellSynchronizationService;

	private final LessonCellEnhancerService lessonCellEnhancerService;

	private final DataLoader dataLoader;

	private final List<LessonCellTimetableMapper> lessonCellTimetableMappers;

	private final Clock clock;

	@Override
	public void internalImportExternalModels(boolean overrideCache) throws ImportException {
		log.info("Starting import");
		prepareDataLoader(overrideCache);
		log.info("Importer have finished preparing data loader");

		List<SelectorGroupDto> groups = queryAllGroups(overrideCache);
		log.info("Found {} groups", groups.size());

		Map<SelectorGroupDto, List<GroupScheduleDto>> schedules =
			queryGroupSchedules(groups, overrideCache);
		log.info("Found {} schedules", schedules.size());

		Map<SelectorGroupDto, List<GroupScheduleDto>> filteredSchedules =
			filterSchedules(schedules);
		log.info("Filtered {} schedules", filteredSchedules.size());

		List<LessonCell> lessonCells = mapSchedules(filteredSchedules);
		log.info("Extracted {} lesson cells", lessonCells.size());

		List<LessonCell> enhancedLessonCells = lessonCellEnhancerService.enhance(lessonCells);
		log.info("Enhanced {} lesson cells", enhancedLessonCells.size());

		rebuildDatabase(enhancedLessonCells);
		log.info("Import finished");
	}

	private void prepareDataLoader(boolean overrideCache) throws ImportException {
		if (overrideCache) {
			try {
				dataLoader.dropCache();
			} catch (IOException e) {
				throw new ImportException("Drop cache error", e);
			}
		}
	}

	private List<SelectorGroupDto> queryAllGroups(boolean overrideCache) throws ImportException {
		try {
			SelectorHolderDto selectorHolderDto =
				objectMapper.readValue(dataLoader.load("selectors", overrideCache), SelectorHolderDto.class);
			return selectorHolderDto.getGroups();
		} catch (IOException e) {
			throw new ImportException("Group load error", e);
		}
	}

	private Map<SelectorGroupDto, List<GroupScheduleDto>> queryGroupSchedules(
		List<SelectorGroupDto> selectorGroupDtos,
		boolean overrideCache
	) throws ImportException {
		Map<SelectorGroupDto, List<GroupScheduleDto>> output = new HashMap<>();
		for (SelectorGroupDto selectorGroupDto : selectorGroupDtos) {
			try {
				String path = "group?type=classes&group=" + selectorGroupDto.getGroupId();
				String rawData = dataLoader.load(path, overrideCache);
				if (rawData.contains("Расписание на найдено")) {
					log.info("Group {} ({}) schedule is absent", selectorGroupDto.getGroupId(), selectorGroupDto.getGroupName());
					continue;
				}
				List<GroupScheduleDto> groupScheduleDtos = objectMapper.readValue(
					rawData,
					objectMapper.getTypeFactory().constructCollectionType(List.class, GroupScheduleDto.class)
				);
				output.put(selectorGroupDto, groupScheduleDtos);
			} catch (IOException e) {
				throw new ImportException(
					String.format(
						"Group %d (%s) schedule load error",
						selectorGroupDto.getGroupId(), selectorGroupDto.getGroupName()
					),
					e
				);
			}
		}
		return output;
	}

	private Map<SelectorGroupDto, List<GroupScheduleDto>> filterSchedules(
		Map<SelectorGroupDto, List<GroupScheduleDto>> schedules
	) {
		LocalDate now = LocalDate.now(clock);
		return EntryStream.of(schedules)
			.mapValues(dtos ->
				StreamEx.of(dtos)
					.filter(dto -> dto.getStart().minusWeeks(3).isBefore(now) && now.isBefore(dto.getFinish()))
					.toList()
			)
			.toImmutableMap();
	}


	private List<LessonCell> mapSchedules(
		Map<SelectorGroupDto, List<GroupScheduleDto>> schedules
	) throws ImportException {
		List<LessonCell> output = new ArrayList<>();
		for (var entry : schedules.entrySet()) {
			SelectorGroupDto selectorGroupDto = entry.getKey();
			List<GroupScheduleDto> groupScheduleDtos = entry.getValue();

			for (GroupScheduleDto groupScheduleDto : groupScheduleDtos) {
				for (LessonDto lessonDto : groupScheduleDto.getLessons()) {
					for (String professor : lessonDto.getProfessors()) {
						LessonTimeDto lessonTimeDto =
							groupScheduleDto.getLessonTimeData().get(lessonDto.getLessonNumber());

						LessonCell lessonCell = LessonCell.builder()
							.ignoreExternalDb(false)
							.build();

						LessonCellTimetableMapperContext ctx = new LessonCellTimetableMapperContext(
							groupScheduleDto,
							lessonDto,
							lessonTimeDto,
							selectorGroupDto,
							professor
						);

						for (LessonCellTimetableMapper mapper : lessonCellTimetableMappers) {
							mapper.map(ctx, lessonCell);
						}

						lessonCell.setExternalId(LessonCellCoordinates.convert(lessonCell).toIdentifier());

						output.add(lessonCell);
					}
				}
			}
		}
		return output;
	}

	protected void rebuildDatabase(List<LessonCell> preparedCells) throws ImportException {
		try {
			lessonCellSynchronizationService.rebuildDatabase(Set.copyOf(preparedCells));
		} catch (Exception e) {
			throw new ImportException(e.getMessage(), e);
		}
	}

	@Override
	public boolean isBusy() {
		return super.isBusy() || lessonCellSynchronizationService.isUpdatingLocalDatabase();
	}
}
