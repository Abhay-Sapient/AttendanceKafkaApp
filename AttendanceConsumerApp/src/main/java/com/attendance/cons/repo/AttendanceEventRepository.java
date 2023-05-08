package com.attendance.cons.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.attendance.cons.entity.AttendanceEvent;
import com.attendance.cons.entity.EventType;

public interface AttendanceEventRepository extends CrudRepository<AttendanceEvent, Integer>{

	List<AttendanceEvent> findByEmpId(Integer empId);
	List<AttendanceEvent> findByEmpIdAndEventTypeAndTimeStampIsBetween(Integer empId, String event, LocalDateTime atStartOfDay,
			LocalDateTime atTime);
	List<AttendanceEvent> findByEmpIdAndEventTypeAndTimeStampIsBetween(Integer empId, EventType swipeIn,
			LocalDateTime atStartOfDay, LocalDateTime atTime);
	List<AttendanceEvent> findByTimeStampIsBetween(LocalDateTime atStartOfDay, LocalDateTime atTime);
}
