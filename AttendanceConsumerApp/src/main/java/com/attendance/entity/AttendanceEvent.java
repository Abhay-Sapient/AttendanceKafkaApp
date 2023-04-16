package com.attendance.entity;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AttendanceEvent {
	@Id
	@GeneratedValue
	private Integer eventId;
	
	@Enumerated(EnumType.STRING)
	private EventType eventType;
	
	@Transient
	private LocalDateTime timeStamp; 
		
	private Integer empId;
	private String empName;
	private String swipeIn; 
	private String swipeOut; 
}
