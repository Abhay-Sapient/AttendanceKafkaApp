package com.attendance.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.attendance.entity.AttendanceEvent;
import com.attendance.entity.AttendanceRequest;
import com.attendance.entity.AttendanceResponse;
import com.attendance.entity.AttendanceType;
import com.attendance.repo.AttendanceEventRepository;
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

	public AttendanceEvent processAttendanceEvent(ConsumerRecord<Integer, String> consumerRecord)
			throws JsonProcessingException {
		AttendanceEvent attendanceEvent = objectMapper.readValue(consumerRecord.value(), AttendanceEvent.class);

		log.info("Event: " + attendanceEvent.getEventType());

		switch (attendanceEvent.getEventType()) {
		case SWIPE_IN:
			attendanceEvent.setSwipeIn(attendanceEvent.getTimeStamp().toString());
			AttendanceEvent attendanceEventFromDB = getAttendanceEventFromDBForSwipeIn(attendanceEvent);
			if (attendanceEventFromDB == null) {
				log.info("Swipe-In repo entry for : " + attendanceEvent.getEmpId());
				repo.save(attendanceEvent);
			}
			break;

		case SWIPE_OUT:

			attendanceEvent.setSwipeOut(attendanceEvent.getTimeStamp().toString());
			AttendanceEvent attendanceEventFromDBOut = getAttendanceEventFromDB(attendanceEvent);
			if (attendanceEventFromDBOut != null) {
				attendanceEventFromDBOut.setSwipeOut(attendanceEvent.getTimeStamp().toString());
				repo.save(attendanceEventFromDBOut);
			} else {
				repo.save(attendanceEvent);
			}
			break;

		default:
			log.info("Invalid event : " + attendanceEvent.getEventType());
			break;
		}
		return attendanceEvent;
	}

	public AttendanceEvent getAttendanceEventFromDBForSwipeIn(AttendanceEvent attendanceEvent) {
		List<AttendanceEvent> attendanceEventsIn = repo.findByEmpId(attendanceEvent.getEmpId());
		if (!attendanceEventsIn.isEmpty()) {
			AttendanceEvent eventEntry = attendanceEventsIn.get(0);
			LocalDate date = LocalDateTime.parse(eventEntry.getSwipeIn()).toLocalDate();
			if (date != null && date.equals(attendanceEvent.getTimeStamp().toLocalDate())) {
				log.info("Existing Swipe-In entry found : " + attendanceEvent.getEmpId());
				return eventEntry;
			}
		}
		return null;
	}

	public AttendanceEvent getAttendanceEventFromDB(AttendanceEvent attendanceEvent) {
		List<AttendanceEvent> attendanceEventsIn = repo.findByEmpId(attendanceEvent.getEmpId());
		if (!attendanceEventsIn.isEmpty()) {
			AttendanceEvent eventEntry = attendanceEventsIn.get(0);
			LocalDate date = eventEntry.getSwipeIn() != null
					? LocalDateTime.parse(eventEntry.getSwipeIn()).toLocalDate()
					: LocalDateTime.parse(eventEntry.getSwipeOut()).toLocalDate();
			if (date.equals(attendanceEvent.getTimeStamp().toLocalDate())) {
				return eventEntry;
			}
		}
		return null;
	}

	public AttendanceResponse getAttendanceData(AttendanceRequest input) {

		AttendanceResponse response = new AttendanceResponse(0, AttendanceType.ABSENT);

		List<AttendanceEvent> attendanceEvents = repo.findByEmpId(input.getEmpId());
		if (!attendanceEvents.isEmpty()) {
			Optional<AttendanceEvent> eventEntryOptional = attendanceEvents.stream()
					.filter(e -> LocalDateTime.parse(e.getSwipeIn()).toLocalDate().equals(input.getInputDate()))
					.findFirst();

			if (eventEntryOptional.isPresent()) {
				AttendanceEvent eventFromDb = eventEntryOptional.get();
				LocalTime swipeInTime = LocalDateTime.parse(eventFromDb.getSwipeIn()).toLocalTime();
				LocalTime swipeOutTime = LocalDateTime.parse(eventFromDb.getSwipeOut()).toLocalTime();

				int totalHours = (int) swipeInTime.until(swipeOutTime, ChronoUnit.HOURS);
				response.setAttendanceHours(totalHours);

				if (totalHours >= 8) {
					response.setAttendanceType(AttendanceType.PRESENT);
				} else if (totalHours >= 4 && totalHours < 8) {
					response.setAttendanceType(AttendanceType.HALF_DAY);
				} else {
					response.setAttendanceType(AttendanceType.ABSENT);
				}
				return response;
			} else {
				log.info("No record found for date " + input.getInputDate());
				return response;
			}
		} else {
			log.info("Employee entry not found. EMP-ID: " + input.getEmpId());
			return response;
		}
	}
}
