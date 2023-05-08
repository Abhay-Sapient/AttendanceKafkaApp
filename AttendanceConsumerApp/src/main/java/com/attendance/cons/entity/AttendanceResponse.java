package com.attendance.cons.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceResponse {

	private Integer empId;
	private Integer attendanceHours;
	private AttendanceType attendanceType;
	
}
