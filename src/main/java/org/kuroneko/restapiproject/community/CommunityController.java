package org.kuroneko.restapiproject.community;

import org.kuroneko.restapiproject.account.CurrentAccount;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.account.domain.UserAuthority;
import org.kuroneko.restapiproject.community.domain.CommunityForm;
import org.kuroneko.restapiproject.errors.ErrorsResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/community", produces = "application/hal+json;charset=UTF-8")
public class CommunityController {

    @GetMapping
    public ResponseEntity showCommunityList(){
        //메인 화면에 커뮤니티별로 게시글을 보여줘야 하는데, qeuryDSL로 Limit을 다중으로 걸면 DB에 부담이 있을지 없을지 모르므로 일단 보류
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity createCommunity(@CurrentAccount Account account, @RequestBody @Valid CommunityForm communityForm,
                                          Errors errorse){
        if (!account.getAuthority().equals(UserAuthority.MASTER)) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }



        if (errorse.hasErrors()) {
            return new ResponseEntity(new ErrorsResource(errorse), HttpStatus.BAD_REQUEST);
        }


        return new ResponseEntity(HttpStatus.CREATED);
    }


    @GetMapping("/{id}")
    public ResponseEntity showCommunity(@PathVariable("id") String id) {


        return ResponseEntity.ok().build();
    }
}
