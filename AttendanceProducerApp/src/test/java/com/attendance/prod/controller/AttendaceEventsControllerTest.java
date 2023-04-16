package com.attendance.prod.controller;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.attendance.prod.AttendanceProducer;
import com.attendance.prod.domain.AttendanceEvent;
import com.attendance.prod.domain.EventType;
import com.fasterxml.jackson.databind.ObjectMapper;


@WebMvcTest(AttendanceEventsController.class)
@AutoConfigureMockMvc
class AttendaceEventsControllerTest {

	@Autowired
    MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    AttendanceProducer attendanceProducer ;

    @Test
    void postAttendanceEvent() throws Exception {

        AttendanceEvent attendanceEvent = AttendanceEvent.builder()
                .eventId(null)
                .empId(1)
                .empName("Abhay")
                .eventType(EventType.SWIPE_IN)
                .build();

        String json = objectMapper.writeValueAsString(attendanceEvent);
        when(attendanceProducer.sendAttendanceEvent(isA(AttendanceEvent.class))).thenReturn(null);

        //expect
        mockMvc.perform(post("/swipe")
        .content(json)
        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

    }
}
