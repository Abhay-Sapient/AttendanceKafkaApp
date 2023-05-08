package com.attendance.cons.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import com.attendance.cons.entity.AttendanceRequest;
import com.attendance.cons.entity.AttendanceResponse;
import com.attendance.cons.entity.AttendanceType;
import com.attendance.cons.service.AttendanceEventsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


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

        AttendanceRequest request = new AttendanceRequest();
        request.setEmpId(1);
        request.setInputDate(LocalDate.parse("2023-04-12"));
        objectMapper.registerModule(new JavaTimeModule());
        String json2 = objectMapper.writeValueAsString(request); 
        
        AttendanceResponse reponse =  new AttendanceResponse(1, 10, AttendanceType.PRESENT);
        ResponseEntity<AttendanceResponse> response2 = new ResponseEntity<AttendanceResponse>(reponse, HttpStatus.OK);       
        when(service.getAttendanceData(request)).thenReturn(response2);

        mockMvc.perform(get("/getAttendace")
        .content(json2)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    }
}
