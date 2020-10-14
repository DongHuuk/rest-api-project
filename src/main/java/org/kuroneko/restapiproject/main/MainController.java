package org.kuroneko.restapiproject.main;

import org.kuroneko.restapiproject.account.AccountController;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.security.Principal;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
public class MainController {

    public static RepresentationModel getIndexLink() {
        var index = new RepresentationModel();
        index.add(linkTo(AccountController.class).withRel("index"));

        return index;
    }


    /*
        Login, Index, docs Index Controller
        Login = OAuth2 사용
     */

    //TODO Principal를 현재 Accouhent Domain으로 바로 받아올 수 있게 설정 및 인증 서버 OAuth2 설정해야 함.
    @GetMapping("/")
    public String index_get(Principal principal) {
        return "index_Get";
    }

<<<<<<< HEAD
    @PostMapping("/")
    public String index_post(Principal principal) {
        return "index_Post";
=======
    //추후에 Event를 설명하는 index 페이지를 따로 만들어서 그쪽을 가리키게끔 만드는 것
    @GetMapping("/api")
    public RepresentationModel apiIndex() {

        return getIndexLink();
>>>>>>> 2610874ce5b33ab3ae254bb431af6eb7170d45d0
    }

    @GetMapping("/re")
    public RedirectView reIndex(){
        return new RedirectView("/");
    }
}
