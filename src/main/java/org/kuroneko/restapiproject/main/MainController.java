package org.kuroneko.restapiproject.main;

import org.apache.coyote.Response;
import org.kuroneko.restapiproject.account.AccountController;
import org.kuroneko.restapiproject.account.AccountService;
import org.kuroneko.restapiproject.account.domain.LoginForm;
import org.kuroneko.restapiproject.community.CommunityDTOResource;
import org.kuroneko.restapiproject.community.CommunityService;
import org.kuroneko.restapiproject.community.domain.CommunityDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.security.Principal;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
public class MainController {

    @Autowired
    private AccountService accountService;
    @Autowired
    private CommunityService communityService;

    public static RepresentationModel getIndexLink(){
        var index = new RepresentationModel();
        index.add(linkTo(AccountController.class).withRel("index"));

        return index;
    }


    /*
        Login, Index, docs Index Controller
        Login = OAuth2 사용
     */

    //TODO Principal를 현재 Accouhent Domain으로 바로 받아올 수 있게 설정 및 인증 서버 OAuth2 설정해야 함.
    @GetMapping(produces = "application/hal+json;charset=UTF-8")
    public ResponseEntity showCommunityList(){
        CommunityDTO communityDTO = this.communityService.findCommunityAndArticles();
        CommunityDTOResource resource = new CommunityDTOResource(communityDTO);

        return new ResponseEntity(resource, HttpStatus.OK);
    }

    @PostMapping("/")
    public String index_post(Principal principal) {
        return "index_Post";
    }

    @PostMapping(value = "/login", produces = "application/json;charset=UTF-8")
    public ResponseEntity login(@RequestBody LoginForm loginForm){
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/error/unauthorized")
    public ResponseEntity unauthorized(){
        return new ResponseEntity(HttpStatus.UNAUTHORIZED);
    }

}
