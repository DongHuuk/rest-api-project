package org.kuroneko.restapiproject.exception;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class IdNotFoundException extends UsernameNotFoundException {
    public IdNotFoundException(String msg) {
        super(msg);
    }
}
