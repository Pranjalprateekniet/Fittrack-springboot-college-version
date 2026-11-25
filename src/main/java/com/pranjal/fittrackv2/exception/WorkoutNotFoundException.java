package com.pranjal.fittrackv2.exception;

public class WorkoutNotFoundException extends RuntimeException {
    public WorkoutNotFoundException(String message){
        super(message);
    }
}
