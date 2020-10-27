package org.kuroneko.restapiproject.account;

import org.kuroneko.restapiproject.errors.ErrorsResource;
import org.kuroneko.restapiproject.token.AccountVO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;

import java.util.Optional;

public class StatusMethod {
    public ResponseEntity<ErrorsResource> returnBadRequestWithErrors(Errors errors) {
        return ResponseEntity.badRequest().body(new ErrorsResource(errors));
    }

    public boolean checkAccountVO(AccountVO accountVO) {
        return accountVO == null;
    }
    public ResponseEntity returnFORBIDDEN(){
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }

    public boolean checkErrors(Errors errors){
        return errors.hasErrors();
    }

    public boolean checkId(Optional<?> objectOptional) {
        return objectOptional.isEmpty();
    }

    public ResponseEntity returnNotFound(){
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }

    public boolean checkEmail(String accountEmail, String accountVOEmail) {
        return accountEmail.equals(accountVOEmail);
    }

    public ResponseEntity returnBadRequest(){
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity returnNOCONTENT(HttpHeaders httpHeaders) {
        return new ResponseEntity(httpHeaders, HttpStatus.NO_CONTENT);
    }
}
