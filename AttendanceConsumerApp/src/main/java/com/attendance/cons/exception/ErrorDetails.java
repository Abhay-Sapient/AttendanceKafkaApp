package com.attendance.cons.exception;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
public class ErrorDetails {
	String message;
	LocalDateTime timeStamp;
}
