package com.attendance.cons.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.attendance.cons.entity.AttendanceEvent;
import com.attendance.cons.entity.AttendanceRequest;
import com.attendance.cons.entity.AttendanceResponse;
import com.attendance.cons.entity.AttendanceType;
import com.attendance.cons.entity.EventType;
import com.attendance.cons.exception.UserNotFoundException;
import com.attendance.cons.repo.AttendanceEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AttendanceEventsService {

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	AttendanceEventRepository repo;

	/**
	 * This Approch will store attendance data in swipeIn and SwipeOut column and
	 * keep updating same data for that day.
	 * 
	 * @param consumerRecord
	 * @return
	 * @throws JsonProcessingException
	 */
	public AttendanceEvent processAttendanceEvent(ConsumerRecord<Integer, String> consumerRecord)
			throws JsonProcessingException {
		AttendanceEvent attendanceEvent = objectMapper.readValue(consumerRecord.value(), AttendanceEvent.class);

		log.info("Event: " + attendanceEvent.getEventType());

		switch (attendanceEvent.getEventType()) {
		case SWIPE_IN:
			attendanceEvent.setSwipeIn(attendanceEvent.getTimeStamp().toString());
			AttendanceEvent attendanceEventFromDB = getAttendanceEventFromDB(attendanceEvent);
			if (attendanceEventFromDB == null) {
				log.info("Swipe-In repo entry for : " + attendanceEvent.getEmpId());
				repo.save(attendanceEvent);
			} else if (attendanceEventFromDB.getSwipeIn() == null) {
				attendanceEventFromDB.setSwipeIn(attendanceEvent.getTimeStamp().toString());
				repo.save(attendanceEventFromDB);
			}
			break;

		case SWIPE_OUT:

			attendanceEvent.setSwipeOut(attendanceEvent.getTimeStamp().toString());
			AttendanceEvent attendanceEventFromDBOut = getAttendanceEventFromDB(attendanceEvent);
			if (attendanceEventFromDBOut != null) {
				attendanceEventFromDBOut.setSwipeOut(attendanceEvent.getTimeStamp().toString());
				repo.save(attendanceEventFromDBOut);
			} else {
				attendanceEvent.setSwipeIn(attendanceEvent.getTimeStamp().toString());
				repo.save(attendanceEvent);
			}
			break;

		default:
			log.info("Invalid event : " + attendanceEvent.getEventType());
			break;
		}
		return attendanceEvent;
	}

	private AttendanceEvent getAttendanceEventFromDB(AttendanceEvent attendanceEvent) {
		List<AttendanceEvent> attendanceEventsIn = repo.findByEmpId(attendanceEvent.getEmpId());
		if (!attendanceEventsIn.isEmpty()) {
			Optional<AttendanceEvent> eventOptional = attendanceEventsIn.stream().filter(e -> {
				LocalDate date = e.getSwipeIn() != null ? LocalDateTime.parse(e.getSwipeIn()).toLocalDate()
						: LocalDateTime.parse(e.getSwipeOut()).toLocalDate();
				return date.equals(attendanceEvent.getTimeStamp().toLocalDate());
			}).findFirst();
			if (eventOptional.isPresent()) {
				return eventOptional.get();
			}
		}
		return null;
	}

	/**
	 * This is for approch 1 where difference between swipeIn and SwipeOut column
	 * time will be returned as attendance hours. If date is not passed, todays date
	 * will be considered by default.
	 * 
	 * @param input
	 * @return
	 * @throws UserNotFoundException
	 */
	public ResponseEntity<AttendanceResponse> getAttendanceData(AttendanceRequest input) throws UserNotFoundException {

		AttendanceResponse response = new AttendanceResponse(input.getEmpId(), 0, AttendanceType.ABSENT);
		LocalDate inputDate = input.getInputDate() != null ? input.getInputDate() : LocalDate.now();
		List<AttendanceEvent> attendanceEvents = repo.findByEmpId(input.getEmpId());
		if (!attendanceEvents.isEmpty()) {
			Optional<AttendanceEvent> eventEntryOptional = attendanceEvents.stream()
					.filter(e -> LocalDateTime.parse(e.getSwipeIn()).toLocalDate().equals(inputDate)).findFirst();

			if (eventEntryOptional.isPresent()) {
				getAttendace(response, eventEntryOptional.get());
				return ResponseEntity.status(HttpStatus.OK).body(response);
			} else {
				log.info("No record found for date " + inputDate);
				throw new UserNotFoundException("No record found for date: " + inputDate);
			}
		} else {
			log.info("Employee entry not found. EMP-ID: " + input.getEmpId());
			throw new UserNotFoundException("User not found with ID: " + input.getEmpId());
		}
	}

	private AttendanceResponse getAttendace(AttendanceResponse response, AttendanceEvent eventFromDb) {
		LocalTime swipeInTime = LocalDateTime.parse(eventFromDb.getSwipeIn()).toLocalTime();
		LocalTime swipeOutTime = null;
		if (eventFromDb.getSwipeOut() != null) {
			swipeOutTime = LocalDateTime.parse(eventFromDb.getSwipeOut()).toLocalTime();
		} else {
			if (LocalDateTime.parse(eventFromDb.getSwipeIn()).toLocalDate().equals(LocalDate.now())) {
				swipeOutTime = LocalTime.now();
			} else {
				swipeOutTime = swipeInTime;
			}
		}
		int totalHours = (int) swipeInTime.until(swipeOutTime, ChronoUnit.HOURS);
		response.setAttendanceHours(totalHours);

		response.setAttendanceType(getAttendanceType(totalHours));
		return response;
	}

	private AttendanceType getAttendanceType(int totalHours) {
		if (totalHours >= 8) {
			return AttendanceType.PRESENT;
		} else if (totalHours >= 4 && totalHours < 8) {
			return AttendanceType.HALF_DAY;
		} else {
			return AttendanceType.ABSENT;
		}
	}

	public AttendanceEvent processAttendanceEventV2(ConsumerRecord<Integer, String> consumerRecord)
			throws JsonProcessingException {
		AttendanceEvent attendanceEvent = objectMapper.readValue(consumerRecord.value(), AttendanceEvent.class);

		log.info("Event: " + attendanceEvent.getEventType());

		repo.save(attendanceEvent);

		return attendanceEvent;
	}

	// APPROACH- 2
	/**
	 * This will take first swipeIn events time and last swipeOut's event time and
	 * return the difference as attendance hours. If date is not passed, todays date
	 * will be considered by default.
	 * 
	 * @param input
	 * @return
	 */
	public ResponseEntity<List<AttendanceResponse>> getAttendanceDataV2(AttendanceRequest input) {
		List<AttendanceResponse> responseList = new ArrayList<>();

		LocalDate inputDate = getInputDate(input);
		if (input != null && input.getEmpId() != null) {
			responseList.add(getAttendanceForEmployee(input.getEmpId(), inputDate));

		} else {

			List<AttendanceEvent> allRecords = repo.findByTimeStampIsBetween(inputDate.atStartOfDay(),
					inputDate.atTime(LocalTime.MAX));

			List<AttendanceEvent> uniqueEmps = allRecords.stream().filter(distinctByKey(k -> k.getEmpId()))
					.collect(Collectors.toList());

			for (AttendanceEvent attendanceEvent : uniqueEmps) {
				responseList.add(getAttendanceForEmployee(attendanceEvent.getEmpId(), inputDate));
			}
		}
		return ResponseEntity.status(HttpStatus.OK).body(responseList);
	}

	private LocalDate getInputDate(AttendanceRequest input) {
		if (input == null) {
			return LocalDate.now();
		} else {
			return input.getInputDate() != null ? input.getInputDate() : LocalDate.now();
		}
	}

	private AttendanceResponse getAttendanceForEmployee(Integer empId, LocalDate inputDate) {
		AttendanceResponse response = new AttendanceResponse(empId, 0, AttendanceType.ABSENT);
		LocalTime swipeInTime;
		LocalTime swipeOutTime;
		List<AttendanceEvent> swipeInEvents = repo.findByEmpIdAndEventTypeAndTimeStampIsBetween(empId,
				EventType.SWIPE_IN, inputDate.atStartOfDay(), inputDate.atTime(LocalTime.MAX));

		if (!CollectionUtils.isEmpty(swipeInEvents)) {
			swipeInTime = swipeInEvents.get(0).getTimeStamp().toLocalTime();
			log.info("swipeInTime: " + swipeInTime);
			List<AttendanceEvent> swipeOutEvents = repo.findByEmpIdAndEventTypeAndTimeStampIsBetween(empId,
					EventType.SWIPE_OUT, inputDate.atStartOfDay(), inputDate.atTime(LocalTime.MAX));
			if (!CollectionUtils.isEmpty(swipeOutEvents)) {
				swipeOutTime = swipeOutEvents.get(swipeOutEvents.size() - 1).getTimeStamp().toLocalTime();
			} else {
				swipeOutTime = swipeInEvents.get(swipeInEvents.size() - 1).getTimeStamp().toLocalTime();
			}
			log.info("swipeOutTime: " + swipeOutTime);
			int totalHours = (int) swipeInTime.until(swipeOutTime, ChronoUnit.HOURS);
			response.setAttendanceHours(totalHours);

			response.setAttendanceType(getAttendanceType(totalHours));
			return response;

		} else {
			log.info("No record found for date " + inputDate);
			return response;
		}
	}

	private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		final Set<Object> seen = new HashSet<>();
		return t -> seen.add(keyExtractor.apply(t));
	}

}
