package com.attendance.cons.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.attendance.controller.AttendaceDataController;
import com.attendance.entity.AttendanceEvent;
import com.attendance.entity.AttendanceRequest;
import com.attendance.entity.EventType;
import com.attendance.service.AttendanceEventsService;
import com.fasterxml.jackson.databind.ObjectMapper;


@WebMvcTest(AttendaceDataController.class)
@AutoConfigureMockMvc
class AttendaceDataControllerTest {

	@Autowired
    MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    AttendanceEventsService service;
    

    @Test
    void getAttendanceData() throws Exception {

        AttendanceEvent attendanceEvent = AttendanceEvent.builder()
                .eventId(null)
                .empId(1)
                .empName("Abhay")
                .eventType(EventType.SWIPE_IN)
                .swipeIn("2023-04-12T20:23:11.969137700")
                .build();

        String json = objectMapper.writeValueAsString(attendanceEvent);
        AttendanceRequest request = new AttendanceRequest();
        request.setEmpId(1);
        request.setInputDate(LocalDate.parse("2023-04-12"));
        when(service.getAttendanceData(request)).thenReturn(null);

        mockMvc.perform(post("/getAttendace")
        .content(json)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    }
}
