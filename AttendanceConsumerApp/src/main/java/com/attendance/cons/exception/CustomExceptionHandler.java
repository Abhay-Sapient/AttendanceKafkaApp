package com.attendance.cons.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler{

//	@ExceptionHandler(Exception.class)
//	public ResponseEntity<ErrorDetails> handleAllExceptions(Exception ex, WebRequest request) {
//		ErrorDetails details = new ErrorDetails(ex.getMessage(), LocalDateTime.now());
//		return new ResponseEntity<>(details, HttpStatus.INTERNAL_SERVER_ERROR); 
//	}
	
	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ErrorDetails> handleUserNotFoundException(Exception ex, WebRequest request) {
		ErrorDetails details = new ErrorDetails(ex.getMessage(), LocalDateTime.now());
		return new ResponseEntity<>(details, HttpStatus.NOT_FOUND); 
	}
}
