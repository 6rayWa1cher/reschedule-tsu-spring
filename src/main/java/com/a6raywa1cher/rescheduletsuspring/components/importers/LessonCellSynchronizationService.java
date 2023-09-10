package com.a6raywa1cher.rescheduletsuspring.components.importers;

import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import com.a6raywa1cher.rescheduletsuspring.models.mapper.LessonCellMapper;
import com.a6raywa1cher.rescheduletsuspring.models.submodels.LessonCellCoordinates;
import com.a6raywa1cher.rescheduletsuspring.service.interfaces.LessonCellService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@AllArgsConstructor
public class LessonCellSynchronizationService {
	private final AtomicBoolean updatingLocalDatabase = new AtomicBoolean();

	private final LessonCellService lessonCellService;
	private final LessonCellMapper mapper;

	/**
	 * Merges local database and new LessonCells.
	 * <p>
	 * This method does a lookup of every LessonCell in the local database.
	 * LessonCell, which origin is an external database, will be overridden by the new
	 * version or dropped if preparedCells don't contain a correspondent LessonCell.
	 * User-made LessonCell will be kept or dropped according to with flags ignoreExternalDb
	 * and ignoreExternalDbHashCode.
	 * <p>
	 * The method does not allow concurrent calls.
	 *
	 * @param preparedCells LessonCells from external database
	 * @throws IllegalStateException if the local database is locked by another call
	 * @throws RuntimeException      if an error occurred while transferring
	 */
	public void rebuildDatabase(Set<LessonCell> preparedCells) {
		// if new LessonCell, save it
		// if updated LessonCell (db contains entity with same id), update it
		// if LessonCell from local db hasn't double from external id, delete it
		if (!this.updatingLocalDatabase.compareAndSet(false, true)) {
			throw new IllegalStateException("Database already in use!");
		}
		try {
			Set<LessonCell> allCellsInDb = lessonCellService.getAll();
			Map<String, LessonCell> idToCellInDb = allCellsInDb.stream()
				.collect(Collectors.toMap(LessonCell::getExternalId, Function.identity()));
			// catch updated LessonCells
			Set<LessonCell> localCellsToMakePatches = preparedCells.stream()
				.filter(lessonCell -> idToCellInDb.containsKey(lessonCell.getExternalId()))
				.collect(Collectors.toSet());
			Set<LessonCell> localUpdatedLessonCell = new HashSet<>();
			Set<LessonCell> localNewLessonCells = new HashSet<>(preparedCells);
			Set<LessonCell> remainingDbCells = new HashSet<>(allCellsInDb);
			for (LessonCell preparedCell : localCellsToMakePatches) {
				LessonCell inDb = idToCellInDb.get(preparedCell.getExternalId());
				remainingDbCells.remove(inDb);
				localNewLessonCells.remove(preparedCell);
				mapper.copyData(preparedCell, inDb);
				localUpdatedLessonCell.add(inDb);
			}
			// User-created LessonCells
			// if ignoreExternalDb and ignoreExternalDbHashCode == null, block external db's identical cell.
			// if ignoreExternalDb and ignoreExternalDbHashCode != null, check cell's hash code. if
			//    hashCode != ignoreExternalDbHashCode, drop user-created. Otherwise block external db's cell.
			// if not ignoreExternalDb and external db contains identical cell, drop user-created.
			//    Otherwise, remain user-created.
			Set<LessonCell> userMadeCells = remainingDbCells.stream()
				.filter(lessonCell -> lessonCell.getCreator() != null)
				.collect(Collectors.toSet());
			remainingDbCells.removeAll(userMadeCells);
			Map<LessonCellCoordinates, LessonCell> userMadeCellsCoordinates = userMadeCells.stream()
				.collect(Collectors.toMap(LessonCellCoordinates::convert, Function.identity()));
			Map<LessonCellCoordinates, List<LessonCell>> importedCellsCoordinates = Stream.concat(localUpdatedLessonCell.stream(),
					localNewLessonCells.stream())
				.collect(Collectors.toMap(LessonCellCoordinates::convert,
					cell -> new LinkedList<>(Collections.singletonList(cell)),
					(l1, l2) -> {
						l1.addAll(l2);
						l2.clear();
						return l1;
					}
				));
			Set<LessonCellCoordinates> coordinatesIntersection = userMadeCellsCoordinates.keySet().stream()
				.filter(importedCellsCoordinates::containsKey)
				.collect(Collectors.toSet());
			for (LessonCellCoordinates intersection : coordinatesIntersection) {
				LessonCell userCreated = userMadeCellsCoordinates.get(intersection);
				List<LessonCell> importedCell = importedCellsCoordinates.get(intersection);
				String importedCellIds = importedCell.stream()
					.map(LessonCell::getExternalId)
					.sorted()
					.collect(Collectors.joining(","));
				int intersectionId = Math.abs(new Random().nextInt());
				String userCreatedId = userCreated.getExternalId();
				log.info("I{}. Intersection of user-created {} with LessonCells: {}",
					intersectionId,
					userCreatedId,
					importedCellIds);
				if (userCreated.getIgnoreExternalDb()) {
					if (userCreated.getIgnoreExternalDbHashCode() != null) {
						String importedCellHashCodeCompilation = importedCell.stream()
							.map(LessonCell::hashCode)
							.sorted()
							.map(hc -> Integer.toString(hc))
							.collect(Collectors.joining(","));
						if (!importedCellHashCodeCompilation.equals(userCreated.getIgnoreExternalDbHashCode())) {
							log.info("I{}. Drop user-created {}. Reason: user-created has outdated block: {} vs actual {}",
								intersectionId,
								userCreatedId,
								userCreated.getIgnoreExternalDbHashCode(),
								importedCellHashCodeCompilation);
							remainingDbCells.add(userCreated);
						} else {
							log.info("I{}. Block and drop LessonCells {}. Reason: user-created {} has hashCode-block {}",
								intersectionId,
								importedCellIds,
								userCreatedId,
								userCreated.getIgnoreExternalDbHashCode());
							localNewLessonCells.removeAll(importedCell);
							localUpdatedLessonCell.removeAll(importedCell);
						}
					} else {
						log.info("I{}. Block and drop LessonCells {}. Reason: user-created {} has absolute-block",
							intersectionId,
							importedCellIds,
							userCreatedId);
						localNewLessonCells.removeAll(importedCell);
						localUpdatedLessonCell.removeAll(importedCell);
					}
				} else {
					log.info("I{}. Drop user-created {}. Reason: not ignoreExternalDb",
						intersectionId,
						userCreatedId);
					remainingDbCells.add(userCreated);
				}
			}
			lessonCellService.saveAll(localUpdatedLessonCell);
			lessonCellService.saveAll(localNewLessonCells);
			lessonCellService.deleteAll(remainingDbCells);
			log.info("Transferring TsuDb data to local db completed, {} added, {} updated, {} deleted, {} total",
				localNewLessonCells.size(),
				localUpdatedLessonCell.size(),
				remainingDbCells.size(),
				lessonCellService.size());
		} catch (Exception e) {
			log.error("Error while transferring", e);
			throw new RuntimeException("Error while transferring", e);
		} finally {
			this.updatingLocalDatabase.set(false);
		}
	}

	public boolean isUpdatingLocalDatabase() {
		return this.updatingLocalDatabase.get();
	}
}
