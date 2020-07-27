package org.kuroneko.restapiproject.main;

import org.kuroneko.restapiproject.account.AccountController;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
public class MainController {

    /*
        Login, Index, docs Index Controller
        Login = OAuth2 사용
     */
    //추후에 Event를 설명하는 index 페이지를 따로 만들어서 그쪽을 가리키게끔 만드는 것
    @GetMapping("/api")
    public RepresentationModel apiIndex(){
        var index = new RepresentationModel();
        index.add(linkTo(AccountController.class).withRel("index"));

        return index;
    }

}
