package com.attendance.repo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.attendance.entity.AttendanceEvent;

public interface AttendanceEventRepository extends CrudRepository<AttendanceEvent, Integer>{

	List<AttendanceEvent> findByEmpId(Integer empId);
}
