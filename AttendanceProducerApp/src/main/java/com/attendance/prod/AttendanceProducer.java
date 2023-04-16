package com.attendance.prod;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.attendance.prod.domain.AttendanceEvent;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDateTime;

import org.apache.kafka.clients.producer.ProducerRecord;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class AttendanceProducer {

	@Autowired
	KafkaTemplate<Integer, String> kafkaTemplate;

	@Autowired
	ObjectMapper objectMapper;
	
	

	String topic = "attendance-topic";

	public ListenableFuture<SendResult<Integer,String>> sendAttendanceEvent(AttendanceEvent event) throws JsonProcessingException {

		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		Integer key = event.getEventId();
		if (event.getTimeStamp() == null) {
			event.setTimeStamp(LocalDateTime.now());
			log.info("Setting current date time");
		}
		String value = objectMapper.writeValueAsString(event);
		ProducerRecord<Integer, String> producerRecord = new ProducerRecord<>(topic, null, key, value, null);
		ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send(producerRecord);
		listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
			@Override
			public void onFailure(Throwable ex) {
				handleOnFailure(key, value, ex);
			}

			@Override
			public void onSuccess(SendResult<Integer, String> result) {
				handleOnSuccess(key, value, result);
			}
		});
		return listenableFuture;
	}

	protected void handleOnFailure(Integer key, String value, Throwable ex) {
		log.error("Failure:" + key + ": " + value + " : Reason" + ex.getMessage());
	}

	protected void handleOnSuccess(Integer key, String value, SendResult<Integer, String> result) {
		log.info("Success: " + key + ": " + value + " Partition: "+result.getRecordMetadata().partition());

	}
}
