package com.attendance.cons.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.attendance.cons.entity.AttendanceRequest;
import com.attendance.cons.entity.AttendanceResponse;
import com.attendance.cons.exception.UserNotFoundException;
import com.attendance.cons.service.AttendanceEventsService;

@RestController
public class AttendaceDataController {

	@Autowired
	AttendanceEventsService service;
	
	@GetMapping("/getAttendace")
	public ResponseEntity<AttendanceResponse> getAttendaceData (@RequestBody AttendanceRequest input) throws UserNotFoundException {
		return service.getAttendanceData(input);
	}
	
	@GetMapping("/v2/getAttendace")
	public ResponseEntity<List<AttendanceResponse>> getAttendaceDataV2 (@RequestBody AttendanceRequest input) {
		return service.getAttendanceDataV2(input);
	}
}
