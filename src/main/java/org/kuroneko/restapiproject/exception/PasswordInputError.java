package org.kuroneko.restapiproject.exception;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class PasswordInputError extends RuntimeException{

    public PasswordInputError() {
        log.error("Not Matching Password and checkPassword!");
    }
}
