# AttendanceKafkaApp

Producer Application will produce all the Swipe_in and out events and will be published in kafka topic. Consumer application will consume those events and store in databse. Then the /getAttendace endpoint will fetch the data based on employee id or date and return the attendace hours.  
