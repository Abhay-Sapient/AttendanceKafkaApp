package com.attendance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.attendance.entity.AttendanceRequest;
import com.attendance.entity.AttendanceResponse;
import com.attendance.service.AttendanceEventsService;

@RestController
public class AttendaceDataController {

	@Autowired
	AttendanceEventsService service;
	
	@PostMapping("/getAttendace")
	public AttendanceResponse getAttendaceData (@RequestBody AttendanceRequest input) {
		return service.getAttendanceData(input);
	}
}
