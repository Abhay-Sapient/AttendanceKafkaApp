package com.attendance.prod.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceEvent {
	private Integer eventId;
	private EventType eventType;
	private LocalDateTime timeStamp; 
	private Integer empId;
	private String empName;	
}
