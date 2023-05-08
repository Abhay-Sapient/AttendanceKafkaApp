package com.attendance.cons.entity;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

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
	
	//these columns are not required to be stored in approach-1
	//@Transient
	@Enumerated(EnumType.STRING)
	private EventType eventType;
	
	//@Transient
	private LocalDateTime timeStamp; 
		
	private Integer empId;
	private String empName;
	private String swipeIn; 
	private String swipeOut; 
}
