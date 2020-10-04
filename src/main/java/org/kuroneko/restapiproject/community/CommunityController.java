package org.kuroneko.restapiproject.community;

import org.kuroneko.restapiproject.account.AccountRepository;
import org.kuroneko.restapiproject.account.CurrentAccount;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.account.domain.UserAuthority;
import org.kuroneko.restapiproject.community.domain.Community;
import org.kuroneko.restapiproject.community.domain.CommunityForm;
import org.kuroneko.restapiproject.errors.ErrorsResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping(value = "/community", produces = "application/hal+json;charset=UTF-8")
public class CommunityController {

    @Autowired private AccountRepository accountRepository;
    @Autowired private CommunityService communityService;
    @Autowired private CommunityRepository communityRepository;

    @GetMapping
    public ResponseEntity showCommunityList(){
        //메인 화면에 커뮤니티별로 게시글을 보여줘야 하는데, qeuryDSL로 Limit을 다중으로 걸면 DB에 부담이 있을지 없을지 모르므로 일단 보류
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity createCommunity(@CurrentAccount Account account, @RequestBody @Valid CommunityForm communityForm,
                                          Errors errors){
        if (!account.getAuthority().equals(UserAuthority.MASTER)) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        Optional<Account> byUsername = this.accountRepository.findByUsername(communityForm.getManager());
        if (byUsername.isEmpty()) {
            errors.rejectValue("manager", "wrong.username", "not Found Username");
        }

        if (errors.hasErrors()) {
            return new ResponseEntity(new ErrorsResource(errors), HttpStatus.BAD_REQUEST);
        }

        this.communityService.createCommunity(communityForm, byUsername.get());
        CommunityResource resource = new CommunityResource();
        resource.add(linkTo(CommunityController.class).slash("CommunityId").withRel("Community Page"));
        resource.add(linkTo(CommunityController.class).withRel("Create Community"));

        return new ResponseEntity(resource, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity findCommunity(@PathVariable("id") String id) {


        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteCommunity(@CurrentAccount Account account, @PathVariable Long id) {
        if (!account.getAuthority().equals(UserAuthority.MASTER)) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        Optional<Community> communityById = this.communityRepository.findById(id);

        if (communityById.isEmpty()) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        this.communityService.deleteCommunity(communityById.get());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(linkTo(CommunityController.class)
                .slash("CommunityId").withRel("get Community").toUri());
        
        return new ResponseEntity(httpHeaders, HttpStatus.NO_CONTENT);
    }
}
