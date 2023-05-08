package com.attendance.prod.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.attendance.prod.AttendanceProducer;
import com.attendance.prod.domain.AttendanceEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
@RestController
public class AttendanceEventsController {

	@Autowired
	AttendanceProducer attendanceProducer;
	
	@PostMapping("/swipe")
	public ResponseEntity<AttendanceEvent> swipeInEvent(@RequestBody AttendanceEvent event) throws JsonProcessingException {
		attendanceProducer.sendAttendanceEvent(event);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(event);
	}
	
}
