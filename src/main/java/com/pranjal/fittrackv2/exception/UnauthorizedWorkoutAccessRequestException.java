package com.pranjal.fittrackv2.exception;

public class UnauthorizedWorkoutAccessRequestException extends RuntimeException{
    public UnauthorizedWorkoutAccessRequestException(String message){
        super(message);
    }
}
