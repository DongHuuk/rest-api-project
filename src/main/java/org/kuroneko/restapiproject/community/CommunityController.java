package org.kuroneko.restapiproject.community;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/community", produces = "application/hal+json;charset=UTF-8")
public class CommunityController {

    @GetMapping
    public ResponseEntity showCommunityList(){
        //메인 화면에 커뮤니티별로 게시글을 보여줘야 하는데, qeuryDSL로 Limit을 다중으로 걸면 DB에 부담이 있을지 없을지 모르므로 일단 보류
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity showCommunity(@PathVariable("id") String id) {


        return ResponseEntity.ok().build();
    }
}
