package com.attendance.prod.producer;


import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import com.attendance.prod.AttendanceProducer;
import com.attendance.prod.domain.AttendanceEvent;
import com.attendance.prod.domain.EventType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@ExtendWith(MockitoExtension.class)
class AttendanceProducerTest {

	@Mock
    KafkaTemplate<Integer,String> kafkaTemplate;

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    AttendanceProducer eventProducer;
    
    @Test
    void sendAttendanceEvent_failure() throws JsonProcessingException, ExecutionException, InterruptedException {
    	 AttendanceEvent attendanceEvent = AttendanceEvent.builder()
                 .eventId(null)
                 .empId(1)
                 .empName("Abhay")
                 .eventType(EventType.SWIPE_IN)
                 .timeStamp(LocalDateTime.now())
                 .build();
        SettableListenableFuture<SendResult<Integer,String>> future = new SettableListenableFuture<SendResult<Integer,String>>();

        future.setException(new RuntimeException("Exception Calling Kafka"));
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);
        ListenableFuture<SendResult<Integer,String>> resp = eventProducer.sendAttendanceEvent(attendanceEvent);
        assertThrows(Exception.class, ()-> resp.get());

    }

    @Test
    void sendAttendanceEvent_success() throws JsonProcessingException, ExecutionException, InterruptedException {
    	objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    	AttendanceEvent attendanceEvent = AttendanceEvent.builder()
                 .eventId(null)
                 .empId(1)
                 .empName("Abhay")
                 .eventType(EventType.SWIPE_IN)
                 .build();

        String record = objectMapper.writeValueAsString(attendanceEvent);
        SettableListenableFuture<SendResult<Integer,String>> future = new SettableListenableFuture<SendResult<Integer,String>>();

        ProducerRecord<Integer, String> producerRecord = new ProducerRecord<Integer, String>("attendance-topic", attendanceEvent.getEventId(),record );
        RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition("attendance-topic", 1),
                1,1,System.currentTimeMillis(), 1, 2);
        SendResult<Integer, String> sendResult = new SendResult<Integer, String>(producerRecord,recordMetadata);

        future.set(sendResult);
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);

        ListenableFuture<SendResult<Integer,String>> listenableFuture =  eventProducer.sendAttendanceEvent(attendanceEvent);

        SendResult<Integer,String> sendResult1 = listenableFuture.get();
        Assertions.assertThat(sendResult1.getRecordMetadata().partition()).isEqualTo(1);

    }
}
