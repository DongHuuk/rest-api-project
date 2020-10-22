package org.kuroneko.restapiproject.exception;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
public class InputNotFoundException extends RuntimeException{

    public InputNotFoundException(IOException e) {
        log.info(e.getClass());
        log.info(e.getMessage());
        log.info(e.getStackTrace());
    }
}
