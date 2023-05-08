package com.attendance.cons.consumer;

	import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
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

import com.attendance.cons.AttendanceEventsConsumer;
import com.attendance.cons.entity.AttendanceEvent;
import com.attendance.cons.entity.EventType;
import com.attendance.cons.repo.AttendanceEventRepository;
import com.attendance.cons.service.AttendanceEventsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

	@ExtendWith(MockitoExtension.class)
	class AttendanceEventsConsumerTest {

		@Mock
		AttendanceEventRepository repo;

		@Spy
		ObjectMapper objectMapper = new ObjectMapper();

		@Mock
		AttendanceEventsService attendanceService;
		
		@InjectMocks
		AttendanceEventsConsumer attendanceConsumer;

		@Test
		void test_consumer() throws JsonProcessingException, ExecutionException, InterruptedException {
			AttendanceEvent attendanceEvent = AttendanceEvent.builder().eventId(null).empId(1).empName("Abhay")
					.eventType(EventType.SWIPE_IN).timeStamp(LocalDateTime.parse("2023-04-13T21:23:11.969137700")).swipeIn("2023-04-12T21:23:11.969137700").build();

			objectMapper.registerModule(new JavaTimeModule());
			objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
			String value = objectMapper.writeValueAsString(attendanceEvent);
			ConsumerRecord<Integer, String> consumerRecord = new ConsumerRecord<Integer, String>("topic", 0, 0, 0, null, 0, 0, null, value, new RecordHeaders(), null);
	//		 doNothing().when(attendanceService).processAttendanceEvent(consumerRecord);
			given(attendanceService.processAttendanceEvent(consumerRecord)).willReturn(attendanceEvent);
		//	verify(attendanceConsumer, times(1)).onMessage(consumerRecord);
			AttendanceEvent response = attendanceConsumer.onMessage(consumerRecord);
			Assertions.assertThat(response.getEmpId()).isEqualTo(1);
		}
		

	}
