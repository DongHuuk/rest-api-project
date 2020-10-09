package org.kuroneko.restapiproject.community;

import org.kuroneko.restapiproject.account.AccountController;
import org.kuroneko.restapiproject.account.AccountRepository;
import org.kuroneko.restapiproject.account.CurrentAccount;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.account.domain.UserAuthority;
import org.kuroneko.restapiproject.article.ArticleRepository;
import org.kuroneko.restapiproject.article.domain.Article;
import org.kuroneko.restapiproject.article.domain.ArticleDTO;
import org.kuroneko.restapiproject.article.domain.ArticleForm;
import org.kuroneko.restapiproject.community.domain.Community;
import org.kuroneko.restapiproject.community.domain.CommunityForm;
import org.kuroneko.restapiproject.community.validation.ArticleValidator;
import org.kuroneko.restapiproject.errors.ErrorsResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping(value = "/community", produces = "application/hal+json;charset=UTF-8")
public class CommunityController {

    @Autowired private AccountRepository accountRepository;
    @Autowired private CommunityService communityService;
    @Autowired private CommunityRepository communityRepository;
    @Autowired private ArticleValidator articleValidator;
    @Autowired private ArticleRepository articleRepository;

    @InitBinder("articleForm")
    public void articleFormValidator(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(this.articleValidator);
    }

    @GetMapping
    public ResponseEntity showCommunityList(){
        //메인 화면에 커뮤니티별로 게시글을 보여줘야 하는데, qeuryDSL로 Limit을 다중으로 걸면 DB에 부담이 있을지 없을지 모르므로 일단 보류
        //커뮤니티들을 보여주면서 그 안에 최신순으로 10개씩 나열해줘야 함
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity createCommunity(@CurrentAccount Account account, @RequestBody @Valid CommunityForm communityForm,
                                          Errors errors){
        if (account == null || !account.getAuthority().equals(UserAuthority.MASTER)) {
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

    //article 리턴 And Page 있어야 할 듯 (유머 게시판에 들어가면 그 글들이랑 next previous 있는 그화면 나와야함
    @GetMapping("/{id}")
    public ResponseEntity findCommunity(@PathVariable Long id,
                                        @PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable,
                                        PagedResourcesAssembler<ArticleDTO> assembler) {
        Optional<Community> communityById = this.communityRepository.findById(id);
        Link selfLink = linkTo(CommunityController.class).slash(id).withRel("get Community And Articles");

        if (communityById.isPresent()) {
            Community community = communityById.get();
            Page<Article> articles = this.articleRepository.findByCommunityWithPageable(community, pageable);
            Page<ArticleDTO> newArticles = this.communityService.wrappingByArticle(articles);
            PagedModel<EntityModel<ArticleDTO>> resultPage = assembler.toModel(newArticles, selfLink);

            return new ResponseEntity(resultPage, HttpStatus.OK);
        }

        Page<Article> articleList = this.articleRepository.findTop10ByOrderByCreateTimeDesc(pageable);
        Page<ArticleDTO> articleDTOS = this.communityService.wrappingByArticle(articleList);
        PagedModel<EntityModel<ArticleDTO>> resultPage = assembler.toModel(articleDTOS, selfLink);

        return new ResponseEntity(resultPage, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity updateCommunity(@CurrentAccount Account account, CommunityForm communityForm ,@PathVariable Long id) {
        if (!account.getAuthority().equals(UserAuthority.MASTER)) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        Optional<Community> communityById = this.communityRepository.findById(id);
        if (communityById.isEmpty()) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        Optional<Account> accountByUsername = this.accountRepository.findByUsername(communityForm.getManager());
        if (accountByUsername.isEmpty()) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        this.communityService.updateCommunity(communityById.get(), communityForm, accountByUsername.get());

        CommunityResource communityResource = new CommunityResource();
        communityResource.add(linkTo(CommunityController.class).slash("/CommunityId").withRel("Community Site"));

        return new ResponseEntity(communityResource, HttpStatus.OK);
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

    @GetMapping("/{id}/article")
    public ResponseEntity findCommunityWithArticle(@CurrentAccount Account account, @PathVariable Long id,
                                                   @PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable,
                                                   PagedResourcesAssembler<ArticleDTO> assembler) {
        Optional<Community> communityRepositoryById = this.communityRepository.findById(id);
        if (communityRepositoryById.isEmpty()) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        if (account != null) {
            Page<ArticleDTO> resultList = this.communityService
                    .createPageableArticleWithAccount(pageable, account, communityRepositoryById.get());
            PagedModel<EntityModel<ArticleDTO>> getArticles = assembler.toModel(resultList,
                    linkTo(CommunityController.class).slash(id + "/article").withRel("get Articles"));
            return new ResponseEntity(getArticles, HttpStatus.OK);
        }else{
            List<Community> resultList = this.communityRepository.findTop10ByOrderByCreateTimeDesc();
            return new ResponseEntity(resultList, HttpStatus.OK);
        }
    }

    @PostMapping("/{id}/article")
    public ResponseEntity createCommunityInArticle(@CurrentAccount Account account, @PathVariable Long id,
                                                   @RequestBody @Valid ArticleForm articleForm, Errors errors) {
        if (errors.hasErrors()) {
            return new ResponseEntity(new ErrorsResource(errors), HttpStatus.BAD_REQUEST);
        }
        if (account == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        Optional<Community> communityById = this.communityRepository.findById(id);
        if (communityById.isEmpty()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        Article article = this.communityService.createCommunityInArticle(articleForm, communityById.get(), account);
        CommunityResource resource = new CommunityResource();
        resource.add(linkTo(AccountController.class).slash(account.getId()).withRel("Account Profile"));
        resource.add(linkTo(CommunityController.class).slash(id).withRel("get Community"));
        resource.add(linkTo(CommunityController.class).slash(id + "/article/" + article.getId())
                .withRel("get Article By Community"));

        return new ResponseEntity(HttpStatus.CREATED);
    }

}
