package com.attendance.cons;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.attendance.cons.entity.AttendanceEvent;
import com.attendance.cons.service.AttendanceEventsService;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AttendanceEventsConsumer {

	@Autowired
	AttendanceEventsService attendaceService;

	@KafkaListener(topics = { "attendance-topic" }, groupId = "attendance-event-group")
	public AttendanceEvent onMessage(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {

		AttendanceEvent event = attendaceService.processAttendanceEvent(consumerRecord);
		log.info("Record consumed " + event.getTimeStamp());
		return event;
	}
	
	@KafkaListener(topics = { "attendance-topic" }, groupId = "attendance-event-group")
	public AttendanceEvent onMessageV2(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {

		AttendanceEvent event = attendaceService.processAttendanceEventV2(consumerRecord);
		log.info("Record consumed V2: " + event.getTimeStamp());
		return event;
	}

}
