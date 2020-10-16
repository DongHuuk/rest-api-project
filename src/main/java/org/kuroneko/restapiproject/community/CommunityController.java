package org.kuroneko.restapiproject.community;

import lombok.extern.slf4j.Slf4j;
import org.kuroneko.restapiproject.account.AccountController;
import org.kuroneko.restapiproject.account.AccountRepository;
import org.kuroneko.restapiproject.account.CurrentAccount;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.account.domain.UserAuthority;
import org.kuroneko.restapiproject.article.ArticleRepository;
import org.kuroneko.restapiproject.article.ArticleResource;
import org.kuroneko.restapiproject.article.domain.Article;
import org.kuroneko.restapiproject.article.domain.ArticleDTO;
import org.kuroneko.restapiproject.article.domain.ArticleForm;
import org.kuroneko.restapiproject.article.domain.ArticleThema;
import org.kuroneko.restapiproject.comments.CommentsRepository;
import org.kuroneko.restapiproject.comments.CommentsService;
import org.kuroneko.restapiproject.comments.domain.CommentForm;
import org.kuroneko.restapiproject.community.domain.Community;
import org.kuroneko.restapiproject.community.domain.CommunityForm;
import org.kuroneko.restapiproject.community.validation.ArticleValidator;
import org.kuroneko.restapiproject.errors.ErrorsResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Slf4j
@RestController
@RequestMapping(value = "/community", produces = "application/hal+json;charset=UTF-8")
public class CommunityController {

    @Autowired private AccountRepository accountRepository;
    @Autowired private CommunityService communityService;
    @Autowired private CommunityRepository communityRepository;
    @Autowired private ArticleValidator articleValidator;
    @Autowired private ArticleRepository articleRepository;
    @Autowired private CommentsRepository commentsRepository;
    @Autowired private CommentsService commentsService;

    private ResponseEntity processingByfindArticleWithCommunity(Link link){
        CommunityResource resource = new CommunityResource();
        resource.add(link);
        return new ResponseEntity(resource, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity findArticleWithCommunityWithThema(Community community, ArticleThema articleThema, Pageable pageable,
                                                             Link link, PagedResourcesAssembler<ArticleDTO> assembler) {
        Page<Article> articles = this.articleRepository.findByCommunityAndDivisionWithPageable(community, articleThema, pageable);
        Page<ArticleDTO> newArticles = this.communityService.wrappingByArticle(articles);
        PagedModel<EntityModel<ArticleDTO>> resultPage = assembler.toModel(newArticles, link);

        return new ResponseEntity(resultPage, HttpStatus.OK);
    }

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
        if (account == null || account.getAuthority().equals(UserAuthority.USER)) {
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
    public ResponseEntity findArticleWithCommunity(@PathVariable Long id, @RequestBody(required = false) Integer cate,
                                        @PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable,
                                        PagedResourcesAssembler<ArticleDTO> assembler) {
        Optional<Community> communityById = this.communityRepository.findById(id);
        Link selfLink = linkTo(CommunityController.class).slash(id).withRel("get Community And Articles");
        Link createCommunityLink = linkTo(CommunityController.class).withRel("create Community");

        if (communityById.isEmpty()) {
            return this.processingByfindArticleWithCommunity(createCommunityLink);
        }

        Community community = communityById.get();

        if (cate == null || cate == 0) {
            Page<Article> articles = this.articleRepository.findByCommunityWithPageable(community, pageable);
            Page<ArticleDTO> newArticles = this.communityService.wrappingByArticle(articles);
            PagedModel<EntityModel<ArticleDTO>> resultPage = assembler.toModel(newArticles, selfLink);

            return new ResponseEntity(resultPage, HttpStatus.OK);
        }

        if (cate == 1000) {
            return findArticleWithCommunityWithThema(community, ArticleThema.HUMOR, pageable, selfLink, assembler);
        } else if (cate == 2000) {
            return findArticleWithCommunityWithThema(community, ArticleThema.CHAT, pageable, selfLink, assembler);
        } else if (cate == 3000) {
            return findArticleWithCommunityWithThema(community, ArticleThema.QUESTION, pageable, selfLink, assembler);
        }else{
            return this.processingByfindArticleWithCommunity(createCommunityLink);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity updateCommunity(@CurrentAccount Account account, @PathVariable("id") Long id,
                                          @RequestBody @Valid CommunityForm communityForm, Errors errors) {
        if (errors.hasErrors()) {
            return new ResponseEntity(new ErrorsResource(errors), HttpStatus.BAD_REQUEST);
        }

        Optional<Community> communityById = this.communityRepository.findById(id);
        if (communityById.isEmpty()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        Optional<Account> accountByUsername = this.accountRepository.findByUsername(communityForm.getManager());
        if(accountByUsername.isEmpty()){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        Community community = communityById.get();
        if (account.getAuthority().equals(UserAuthority.USER)
                || !community.getManager().equals(account)
                || community.getManager().getAuthority().equals(UserAuthority.USER)) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        this.communityService.updateCommunity(community, communityForm, accountByUsername.get());

        CommunityResource communityResource = new CommunityResource();
        communityResource.add(linkTo(CommunityController.class).slash("/CommunityId").withRel("Community Site"));

        return new ResponseEntity(communityResource, HttpStatus.OK);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity deleteCommunity(@CurrentAccount Account account, @PathVariable Long id) {
        if (account.getAuthority().equals(UserAuthority.USER)) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }
        Optional<Community> communityById = this.communityRepository.findById(id);

        if (communityById.isEmpty()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        this.communityService.deleteCommunity(communityById.get());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(linkTo(CommunityController.class).withRel("root").toUri());
        
        return new ResponseEntity(httpHeaders, HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{id}/article")
    public ResponseEntity createArticleIntCommunity(@CurrentAccount Account account, @PathVariable Long id,
                                                   @RequestBody @Valid ArticleForm articleForm, Errors errors) {
        if (errors.hasErrors()) {
            return new ResponseEntity(new ErrorsResource(errors), HttpStatus.BAD_REQUEST);
        }
        if (account == null) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        Optional<Community> communityById = this.communityRepository.findById(id);
        if (communityById.isEmpty()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        Article article = this.communityService.createArticleInCommunity(articleForm, communityById.get(), account);
        CommunityResource resource = new CommunityResource();
        resource.add(linkTo(AccountController.class).slash(account.getId()).withRel("Account Profile"));
        resource.add(linkTo(CommunityController.class).slash(id).withRel("get Community"));
        resource.add(linkTo(CommunityController.class).slash(id + "/article/" + article.getId())
                .withRel("get Article By Community"));

        return new ResponseEntity(resource, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/article/{articleId}")
    public ResponseEntity findArticleInCommunity(@PathVariable("id") Long communityId,
                                      @PathVariable("articleId") Long articleId) {
        Optional<Community> communityRepositoryById = this.communityRepository.findById(communityId);
        Optional<Article> articleRepositoryById = this.articleRepository.findById(articleId);
        if (communityRepositoryById.isEmpty() || articleRepositoryById.isEmpty()) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        Article article = articleRepositoryById.get();
        if (!article.getCommunity().equals(communityRepositoryById.get())) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        ArticleResource resource = new ArticleResource(article);
        resource.add(WebMvcLinkBuilder.linkTo(CommunityController.class).slash(article.getCommunity().getId()
                + "/article").withRel("create Article In Community"));

        return new ResponseEntity(resource, HttpStatus.OK);
    }

    @DeleteMapping("/{id}/article/{articleId}")
    public ResponseEntity deleteArticleInCommunity(@CurrentAccount Account account,
                                                    @PathVariable("id") Long communityId,
                                                    @PathVariable("articleId") Long articleId) {
        if (account == null) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        Optional<Community> communityRepositoryById = this.communityRepository.findById(communityId);
        Optional<Article> articleRepositoryById = this.articleRepository.findById(articleId);
        if (communityRepositoryById.isEmpty() || articleRepositoryById.isEmpty()) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        Article article = articleRepositoryById.get();
        Community community = communityRepositoryById.get();
        if (!article.getCommunity().equals(community)) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        this.communityService.deleteArticleInCommunity(article, community, account);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(linkTo(CommunityController.class).slash(community.getId())
                .withRel("get Community").toUri());

        return new ResponseEntity(httpHeaders, HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}/article/{articleId}/modify")
    public ResponseEntity findArticleWithCommunity(@CurrentAccount Account account,
                                                     @PathVariable("id") Long communityId,
                                                     @PathVariable("articleId") Long articleId) {
        if (account == null) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        Optional<Community> communityRepositoryById = this.communityRepository.findById(communityId);
        Optional<Article> articleRepositoryById = this.articleRepository.findById(articleId);
        if (communityRepositoryById.isEmpty() || articleRepositoryById.isEmpty()) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        Article article = articleRepositoryById.get();
        if (!article.getCommunity().equals(communityRepositoryById.get()) || !article.getAccount().equals(account)) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        ArticleResource resource = new ArticleResource(article);
        resource.add(WebMvcLinkBuilder.linkTo(CommunityController.class).slash(article.getCommunity().getId()
                + "/article").withRel("create Article In Community"));

        return new ResponseEntity(resource, HttpStatus.OK);
    }

    @PutMapping("/{id}/article/{articleId}/modify")
    public ResponseEntity updateArticleWithCommunity(@CurrentAccount Account account, @PathVariable("id") Long communityId,
                                                   @PathVariable("articleId") Long articleId,
                                                     @RequestBody @Valid ArticleForm articleForm, Errors errors) {
        if (account == null) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        if (errors.hasErrors()) {
            return new ResponseEntity(new ErrorsResource(errors), HttpStatus.BAD_REQUEST);
        }

        Optional<Community> communityRepositoryById = this.communityRepository.findById(communityId);
        Optional<Article> articleRepositoryById = this.articleRepository.findById(articleId);
        if (communityRepositoryById.isEmpty() || articleRepositoryById.isEmpty()) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        Article article = articleRepositoryById.get();
        Community community = communityRepositoryById.get();
        if (!article.getCommunity().equals(community) || !article.getAccount().equals(account)) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        Article newArticle = this.communityService.updateArticleInCommunity(articleForm, community, article);

        ArticleResource resource = new ArticleResource();
        resource.add(WebMvcLinkBuilder.linkTo(CommunityController.class)
                .slash(article.getCommunity().getId() + "/article/" + newArticle.getId())
                .withRel("show Article In Community"));
        resource.add(WebMvcLinkBuilder.linkTo(CommunityController.class)
                .slash(article.getCommunity().getId() + "/article")
                .withRel("create Article In Community"));

        return new ResponseEntity(resource, HttpStatus.OK);
    }

    @PostMapping("/{id}/article/{articleId}/comments")
    @Transactional
    public ResponseEntity saveComment(@CurrentAccount Account account, @PathVariable("id") Long communityId,
                                      @PathVariable("articleId") Long articleId,
                                      @RequestBody @Valid CommentForm commentForm, Errors errors) {
        if (account == null) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        if (errors.hasErrors()) {
            return new ResponseEntity(new ErrorsResource(errors), HttpStatus.BAD_REQUEST);
        }

        Optional<Community> communityRepositoryById = this.communityRepository.findById(communityId);
        Optional<Article> articleRepositoryById = this.articleRepository.findById(articleId);
        if (communityRepositoryById.isEmpty() || articleRepositoryById.isEmpty()) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        Article article = articleRepositoryById.get();
        Community community = communityRepositoryById.get();
        if (!article.getCommunity().equals(community)) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        this.commentsService.createComments(commentForm, account, article);

        return new ResponseEntity(HttpStatus.CREATED);
    }

}
