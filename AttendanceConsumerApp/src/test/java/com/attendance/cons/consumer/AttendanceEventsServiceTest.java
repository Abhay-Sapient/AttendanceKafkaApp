package com.attendance.cons.consumer;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.attendance.entity.AttendanceEvent;
import com.attendance.entity.AttendanceRequest;
import com.attendance.entity.AttendanceResponse;
import com.attendance.entity.AttendanceType;
import com.attendance.entity.EventType;
import com.attendance.repo.AttendanceEventRepository;
import com.attendance.service.AttendanceEventsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@ExtendWith(MockitoExtension.class)
class AttendanceEventsServiceTest {

	@Mock
	AttendanceEventRepository repo;

	@Spy
	ObjectMapper objectMapper = new ObjectMapper();

	@InjectMocks
	AttendanceEventsService attendanceService;

	@Test
	void test_swipeIn() throws JsonProcessingException, ExecutionException, InterruptedException {
		AttendanceEvent attendanceEvent = AttendanceEvent.builder().eventId(null).empId(1).empName("Abhay")
				.eventType(EventType.SWIPE_IN).timeStamp(LocalDateTime.parse("2023-04-13T21:23:11.969137700")).swipeIn("2023-04-12T21:23:11.969137700").build();

		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		String value = objectMapper.writeValueAsString(attendanceEvent);
		List<AttendanceEvent> listOfEvent = List.of(attendanceEvent);
		ConsumerRecord<Integer, String> consumerRecord = new ConsumerRecord<Integer, String>("topic", 0, 0, 0, null, 0, 0, null, value, new RecordHeaders(), null);
		given(repo.findByEmpId(attendanceEvent.getEmpId())).willReturn(listOfEvent);	
	//	given(objectMapper.readValue(Mockito.any(String.class), AttendanceEvent.class)).willReturn(attendanceEvent);
		attendanceService.processAttendanceEvent(consumerRecord);
		attendanceEvent.setSwipeIn(attendanceEvent.getTimeStamp().toString());
		verify(repo, times(1)).save(attendanceEvent);
	
	}
	
	@Test
	void test_swipeOut() throws JsonProcessingException, ExecutionException, InterruptedException {
		AttendanceEvent attendanceEvent = AttendanceEvent.builder().eventId(null).empId(1).empName("Abhay")
				.eventType(EventType.SWIPE_OUT).timeStamp(LocalDateTime.now()).swipeOut(LocalDateTime.now().toString()).build();

		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		String value = objectMapper.writeValueAsString(attendanceEvent);
		List<AttendanceEvent> listOfEvent = List.of(attendanceEvent);
		ConsumerRecord<Integer, String> consumerRecord = new ConsumerRecord<Integer, String>("topic", 0, 0, 0, null, 0, 0, null, value, new RecordHeaders(), null);
		given(repo.findByEmpId(attendanceEvent.getEmpId())).willReturn(listOfEvent);
	//	given(objectMapper.readValue(Mockito.any(String.class), AttendanceEvent.class)).willReturn(attendanceEvent);
		attendanceService.processAttendanceEvent(consumerRecord);
		verify(repo, times(1)).save(attendanceEvent);
	
	}
	
	@Test
	void test_swipeOutNew() throws JsonProcessingException, ExecutionException, InterruptedException {
		AttendanceEvent attendanceEvent = AttendanceEvent.builder().eventId(null).empId(1).empName("Abhay")
				.eventType(EventType.SWIPE_OUT).timeStamp(LocalDateTime.now()).swipeIn(LocalDateTime.now().toString()).build();

		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		String value = objectMapper.writeValueAsString(attendanceEvent);
		List<AttendanceEvent> listOfEvent = List.of(attendanceEvent);
		ConsumerRecord<Integer, String> consumerRecord = new ConsumerRecord<Integer, String>("topic", 0, 0, 0, null, 0, 0, null, value, new RecordHeaders(), null);
		given(repo.findByEmpId(attendanceEvent.getEmpId())).willReturn(listOfEvent);
	//	given(objectMapper.readValue(Mockito.any(String.class), AttendanceEvent.class)).willReturn(attendanceEvent);
		attendanceService.processAttendanceEvent(consumerRecord);
		verify(repo, times(1)).save(attendanceEvent);
	
	}
	
    @Test
    void test_getAttendanceData() throws Exception {

        AttendanceEvent attendanceEvent = AttendanceEvent.builder()
                .eventId(null)
                .empId(1)
                .empName("Abhay")
                .eventType(EventType.SWIPE_IN)
                .swipeIn("2023-04-12T20:23:11.969137700")
                .build();
        
		AttendanceEvent attendanceEventFromDB = AttendanceEvent.builder().eventId(null).empId(1).empName("Abhay")
				.eventType(EventType.SWIPE_IN).timeStamp(LocalDateTime.now()).swipeIn("2023-04-12T10:23:11.969137700").swipeOut("2023-04-12T20:23:11.969137700").build();
		
        AttendanceRequest request = new AttendanceRequest();
        request.setEmpId(1);
        request.setInputDate(LocalDate.parse("2023-04-12"));
        AttendanceResponse response = new AttendanceResponse();
        response.setAttendanceHours(10);
        response.setAttendanceType(AttendanceType.PRESENT);
        List<AttendanceEvent> listOfEvent = List.of(attendanceEventFromDB);
        given(repo.findByEmpId(attendanceEvent.getEmpId())).willReturn(listOfEvent);
        response = attendanceService.getAttendanceData(request);
        Assertions.assertThat(response.getAttendanceHours()).isEqualTo(10);

    }
    
    
    @Test
    void test_getAttendanceData_DateNotFound() throws Exception {

        AttendanceEvent attendanceEvent = AttendanceEvent.builder()
                .eventId(null)
                .empId(1)
                .empName("Abhay")
                .eventType(EventType.SWIPE_IN)
                .swipeIn("2023-04-12T20:23:11.969137700")
                .build();
        
		AttendanceEvent attendanceEventFromDB = AttendanceEvent.builder().eventId(null).empId(1).empName("Abhay")
				.eventType(EventType.SWIPE_IN).timeStamp(LocalDateTime.now()).swipeIn("2023-04-12T10:23:11.969137700").swipeOut("2023-04-12T20:23:11.969137700").build();
		
        AttendanceRequest request = new AttendanceRequest();
        request.setEmpId(1);
        request.setInputDate(LocalDate.parse("2023-04-10"));
        AttendanceResponse response = new AttendanceResponse();
        response.setAttendanceHours(0);
        response.setAttendanceType(AttendanceType.ABSENT);
        List<AttendanceEvent> listOfEvent = List.of(attendanceEventFromDB);
        given(repo.findByEmpId(attendanceEvent.getEmpId())).willReturn(listOfEvent);
        response = attendanceService.getAttendanceData(request);
        Assertions.assertThat(response.getAttendanceHours()).isZero();

    }
    
    @Test
    void test_getAttendanceData_EmpNotFound() throws Exception {

        AttendanceRequest request = new AttendanceRequest();
        request.setEmpId(11);
        request.setInputDate(LocalDate.parse("2023-04-12"));
        AttendanceResponse response = new AttendanceResponse();
        response.setAttendanceHours(0);
        response.setAttendanceType(AttendanceType.ABSENT);
        given(repo.findByEmpId(11)).willReturn(List.of());
        response = attendanceService.getAttendanceData(request);
        Assertions.assertThat(response.getAttendanceHours()).isZero();

    }
    
}
