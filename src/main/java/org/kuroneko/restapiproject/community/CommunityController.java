package org.kuroneko.restapiproject.community;

import lombok.extern.slf4j.Slf4j;
import org.kuroneko.restapiproject.account.AccountController;
import org.kuroneko.restapiproject.account.AccountRepository;
import org.kuroneko.restapiproject.account.StatusMethod;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.account.domain.UserAuthority;
import org.kuroneko.restapiproject.article.ArticleDTOResource;
import org.kuroneko.restapiproject.article.ArticleRepository;
import org.kuroneko.restapiproject.article.ArticleService;
import org.kuroneko.restapiproject.article.domain.*;
import org.kuroneko.restapiproject.comments.CommentsDTOResource;
import org.kuroneko.restapiproject.comments.CommentsRepository;
import org.kuroneko.restapiproject.comments.CommentsService;
import org.kuroneko.restapiproject.comments.domain.CommentForm;
import org.kuroneko.restapiproject.comments.domain.Comments;
import org.kuroneko.restapiproject.community.domain.Community;
import org.kuroneko.restapiproject.community.domain.CommunityForm;
import org.kuroneko.restapiproject.token.AccountVO;
import org.kuroneko.restapiproject.token.CurrentAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Slf4j
@RestController
@RequestMapping(value = "/community", produces = "application/hal+json;charset=UTF-8")
public class CommunityController extends StatusMethod {

    @Autowired private AccountRepository accountRepository;
    @Autowired private CommunityService communityService;
    @Autowired private CommunityRepository communityRepository;
    @Autowired private ArticleRepository articleRepository;
    @Autowired private CommentsRepository commentsRepository;
    @Autowired private CommentsService commentsService;
    @Autowired private ArticleService articleService;

    private ResponseEntity findArticleWithCommunityWithType(Community community, ArticleThema articleThema, Pageable pageable,
                                                            Link link, PagedResourcesAssembler<ArticleDTOByMainPage> assembler) {
        Page<Article> articles = this.articleRepository.findByCommunityAndDivisionWithPageable(community, articleThema, pageable);
        Page<ArticleDTOByMainPage> newArticles = this.communityService.articleWrappingByArticleDTOByMainPage(articles);
        PagedModel<EntityModel<ArticleDTOByMainPage>> resultPage = assembler.toModel(newArticles, link);
        resultPage.add(linkTo(CommunityController.class)
                .slash("/" + community.getId() + "/article").withRel("create_Article_In_Community"));
        resultPage.add(AccountController.getDOSCURL("/docs/index.html#resources-Community-get-WithType"));
        return new ResponseEntity(resultPage, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity createCommunity(@CurrentAccount AccountVO accountVO, @RequestBody @Valid CommunityForm communityForm,
                                          Errors errors){
        if (accountVO == null || accountVO.getAuthority().equals(UserAuthority.USER)) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        Optional<Account> byUsername = this.accountRepository.findByUsername(communityForm.getManager());
        if (this.checkId(byUsername)) return this.returnNotFound();

        if (this.checkErrors(errors)) return this.returnBadRequestWithErrors(errors);

        Community community = this.communityService.createCommunity(communityForm, byUsername.get());
        CommunityResource resource = new CommunityResource();
        resource.add(linkTo(CommunityController.class).slash("/" + community.getId()).withRel("Community Site"));
        resource.add(AccountController.getDOSCURL("/docs/index.html#resources-Community-create"));

        return new ResponseEntity(resource, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity findArticleWithCommunity(@PathVariable Long id, @RequestParam(required = false, name = "cate") Integer cate,
                                        @PageableDefault(value = 20, sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable,
                                        PagedResourcesAssembler<ArticleDTOByMainPage> assembler) {
        Optional<Community> communityById = this.communityRepository.findById(id);
        Link selfLink = linkTo(CommunityController.class).slash(id).withRel("get Community And Articles");

        if (this.checkId(communityById)) return this.returnBadRequest();

        Community community = communityById.get();

        /*
            TODO article의 comment 부분은 필요없고 count 숫자만 필요함
            프론트에서 사용하는 값은 다음과 같다.
            article - Number, division, title, writer(account), date, 해당 article을 호출 하기 위한 Id 값
            comment - 해당 article에 속해있는 comment의 총 갯수
        */
        if (cate == null || cate == 0) {
            Page<Article> articles = this.articleRepository.findByCommunityWithPageable(community, pageable);
            Page<ArticleDTOByMainPage> newArticles = this.communityService.articleWrappingByArticleDTOByMainPage(articles);
            PagedModel<EntityModel<ArticleDTOByMainPage>> resultPage = assembler.toModel(newArticles, selfLink);
            resultPage.add(linkTo(CommunityController.class)
                    .slash("/" + id + "/article").withRel("create_Article_In_Community"));
            resultPage.add(AccountController.getDOSCURL("/docs/index.html#resources-Community-get"));
            return new ResponseEntity(resultPage, HttpStatus.OK);
        }

        if (cate == 1000) {
            return findArticleWithCommunityWithType(community, ArticleThema.HUMOR, pageable, selfLink, assembler);
        } else if (cate == 2000) {
            return findArticleWithCommunityWithType(community, ArticleThema.CHAT, pageable, selfLink, assembler);
        } else if (cate == 3000) {
            return findArticleWithCommunityWithType(community, ArticleThema.QUESTION, pageable, selfLink, assembler);
        }else{
            return this.returnBadRequest();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity updateCommunity(@CurrentAccount AccountVO accountVO, @PathVariable("id") Long id,
                                          @RequestBody @Valid CommunityForm communityForm, Errors errors) {
        if (this.checkErrors(errors)) return returnBadRequestWithErrors(errors);

        Optional<Community> communityById = this.communityRepository.findById(id);
        if (this.checkId(communityById)) return this.returnNotFound();

        Optional<Account> accountByUsername = this.accountRepository.findByUsername(communityForm.getManager());
        if(this.checkId(accountByUsername)) return this.returnNotFound();

        Community community = communityById.get();
        if (accountVO == null || accountVO.getAuthority().equals(UserAuthority.USER)
                || !community.getManager().getEmail().equals(accountVO.getEmail())
                || community.getManager().getAuthority().equals(UserAuthority.USER)) {
            return this.returnFORBIDDEN();
        }

        this.communityService.updateCommunity(community, communityForm, accountByUsername.get());

        CommunityResource resource = new CommunityResource();
        resource.add(linkTo(CommunityController.class)
                .slash("/" + community.getId()).withRel("Community Site"));
        resource.add(AccountController.getDOSCURL("/docs/index.html#resources-Community-update"));

        return new ResponseEntity(resource, HttpStatus.OK);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity deleteCommunity(@CurrentAccount AccountVO accountVO, @PathVariable Long id) {
        if (this.checkAccountVO(accountVO)) return returnFORBIDDEN();
        if (accountVO.getAuthority().equals(UserAuthority.USER)) return returnFORBIDDEN();

        Optional<Community> communityById = this.communityRepository.findById(id);
        if (this.checkId(communityById)) return this.returnNotFound();

        this.communityService.deleteCommunity(communityById.get());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(linkTo(CommunityController.class).withRel("Home Page").toUri());

        return this.returnNOCONTENT(httpHeaders);
    }

    @PostMapping("/{id}/article")
    public ResponseEntity createArticleIntCommunity(@CurrentAccount AccountVO accountVO, @PathVariable Long id,
                                                   @RequestBody @Valid ArticleForm articleForm, Errors errors) {
        if (this.checkAccountVO(accountVO)) return this.returnFORBIDDEN();
        if (this.checkErrors(errors)) return this.returnBadRequestWithErrors(errors);

        Optional<Community> communityById = this.communityRepository.findById(id);
        if (this.checkId(communityById)) return this.returnNotFound();

        Account account = this.accountRepository.findByEmail(accountVO.getEmail()).orElseThrow();
        Article article = this.communityService.createArticleInCommunity(articleForm, communityById.get(), account);

        CommunityResource resource = new CommunityResource();
        resource.add(linkTo(AccountController.class).slash(account.getId()).withRel("self"));
        resource.add(linkTo(CommunityController.class).slash(id).withRel("Community Site"));
        resource.add(linkTo(CommunityController.class).slash(id + "/article/" + article.getId())
                .withRel("get Article By Community"));
        resource.add(AccountController.getDOSCURL("/docs/index.html#resources-Community-Article-create"));

        return new ResponseEntity(resource, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/article/{articleId}")
    public ResponseEntity findArticleInCommunity(@CurrentAccount AccountVO accountVO, @PathVariable("id") Long communityId,
                                      @PathVariable("articleId") Long articleId) {
        //TODO 로그인 여부에 따라 삭제, 수정 가능하게 만드는게 백엔드에서 해야 하는 것이면 수정
        Optional<Community> communityRepositoryById = this.communityRepository.findById(communityId);
        Optional<Article> articleRepositoryById = this.articleRepository.findById(articleId);
        if (this.checkId(communityRepositoryById) || this.checkId(articleRepositoryById)) return this.returnNotFound();

        Article article = articleRepositoryById.get();
        if (!article.getCommunity().equals(communityRepositoryById.get())) return this.returnBadRequest();

        ArticleDTO articleDTO = this.articleService.wrappingArticleByArticleDTO(article);
        ArticleDTOResource resource = new ArticleDTOResource(articleDTO);
        resource.add(linkTo(CommunityController.class).slash(communityId).withRel("Community_Site"));
        resource.add(AccountController.getDOSCURL("/docs/index.html#resources-Community-Article-get"));

        return new ResponseEntity(resource, HttpStatus.OK);
    }

    @DeleteMapping("/{id}/article/{articleId}")
    public ResponseEntity deleteArticleInCommunity(@CurrentAccount AccountVO accountVO,
                                                    @PathVariable("id") Long communityId,
                                                    @PathVariable("articleId") Long articleId) {
        if (this.checkAccountVO(accountVO)) return this.returnFORBIDDEN();

        Optional<Community> communityRepositoryById = this.communityRepository.findById(communityId);
        Optional<Article> articleRepositoryById = this.articleRepository.findById(articleId);
        if (this.checkId(communityRepositoryById) || this.checkId(articleRepositoryById))
            return this.returnNotFound();

        Article article = articleRepositoryById.get();
        Community community = communityRepositoryById.get();
        if (!article.getCommunity().equals(community)) return this.returnBadRequest();

        this.communityService.deleteArticleInCommunity(article, community, this.accountRepository.findByEmail(accountVO.getEmail()).get());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(linkTo(CommunityController.class).slash(community.getId())
                .withRel("get Community").toUri());

        return this.returnNOCONTENT(httpHeaders);
    }

    @GetMapping("/{id}/article/{articleId}/modify")
    public ResponseEntity findArticleWithCommunity(@CurrentAccount AccountVO accountVO,
                                                     @PathVariable("id") Long communityId,
                                                     @PathVariable("articleId") Long articleId) {
        if (this.checkAccountVO(accountVO)) return this.returnFORBIDDEN();

        Optional<Community> communityRepositoryById = this.communityRepository.findById(communityId);
        Optional<Article> articleRepositoryById = this.articleRepository.findById(articleId);
        if (this.checkId(communityRepositoryById) || this.checkId(articleRepositoryById)) {
            return this.returnNotFound();
        }

        Article article = articleRepositoryById.get();
        if (!article.getCommunity().equals(communityRepositoryById.get())
                || !article.getAccount().getEmail().equals(accountVO.getEmail())) {
            return this.returnBadRequest();
        }

        ArticleDTO articleDTO = this.articleService.wrappingArticleByArticleDTO(article);
        ArticleDTOResource resource = new ArticleDTOResource(articleDTO);
        resource.add(linkTo(AccountController.class).slash(article.getAccount().getId()).withRel("self"));
        resource.add(linkTo(CommunityController.class).slash(communityId).withRel("Community Site"));
        resource.add(linkTo(CommunityController.class).slash(communityId + "/article/" + article.getId())
                .withRel("get Article By Community"));
        resource.add(AccountController.getDOSCURL("/docs/index.html#resources-Community-updatePage"));

        return new ResponseEntity(resource, HttpStatus.OK);
    }

    @PutMapping("/{id}/article/{articleId}/modify")
    public ResponseEntity updateArticleWithCommunity(@CurrentAccount AccountVO accountVO, @PathVariable("id") Long communityId,
                                                   @PathVariable("articleId") Long articleId,
                                                     @RequestBody @Valid ArticleForm articleForm, Errors errors) {
        if (this.checkAccountVO(accountVO)) return this.returnFORBIDDEN();
        if (this.checkErrors(errors)) return this.returnBadRequestWithErrors(errors);

        Optional<Community> communityRepositoryById = this.communityRepository.findById(communityId);
        Optional<Article> articleRepositoryById = this.articleRepository.findById(articleId);
        if (this.checkId(communityRepositoryById) || this.checkId(articleRepositoryById)) return this.returnNotFound();

        Article article = articleRepositoryById.get();
        Community community = communityRepositoryById.get();
        if (!article.getCommunity().equals(community)
                || !article.getAccount().getEmail().equals(accountVO.getEmail())) {
            return this.returnBadRequest();
        }

        Article newArticle = this.communityService.updateArticleInCommunity(articleForm, community, article);

        ArticleDTO articleDTO = this.articleService.wrappingArticleByArticleDTO(newArticle);
        ArticleDTOResource resource = new ArticleDTOResource();
        resource.add(linkTo(AccountController.class).slash(article.getAccount().getId()).withRel("self"));
        resource.add(linkTo(CommunityController.class).slash(communityId).withRel("Community Site"));
        resource.add(linkTo(CommunityController.class).slash(communityId + "/article/" + article.getId())
                .withRel("get Article By Community"));
        resource.add(AccountController.getDOSCURL("/docs/index.html#resources-Community-update"));

        return new ResponseEntity(resource, HttpStatus.OK);
    }

    @PostMapping("/{id}/article/{articleId}/comments")
    @Transactional
    public ResponseEntity createComment(@CurrentAccount AccountVO accountVO, @PathVariable("id") Long communityId,
                                      @PathVariable("articleId") Long articleId,
                                      @RequestBody @Valid CommentForm commentForm, Errors errors) {
        if (this.checkAccountVO(accountVO)) return this.returnFORBIDDEN();
        if (this.checkErrors(errors)) return this.returnBadRequestWithErrors(errors);

        Optional<Community> communityRepositoryById = this.communityRepository.findById(communityId);
        Optional<Article> articleRepositoryById = this.articleRepository.findById(articleId);
        if (communityRepositoryById.isEmpty() || articleRepositoryById.isEmpty()) return this.returnNotFound();

        Article article = articleRepositoryById.get();
        Community community = communityRepositoryById.get();
        if (!article.getCommunity().equals(community)) return this.returnBadRequest();

        Comments comments = this.commentsService.createComments(commentForm, this.accountRepository.findByEmail(accountVO.getEmail()).get(), article);
//        CommentsDTO commentsDTO = this.commentsService.wrappingComments(comments, article);
        CommentsDTOResource resource = new CommentsDTOResource();
        resource.add(linkTo(AccountController.class).slash(article.getAccount().getId()).withRel("self"));
        resource.add(linkTo(CommunityController.class).slash(communityId).withRel("Community Site"));
        resource.add(linkTo(CommunityController.class).slash("/" + communityId + "/article/" + articleId)
                .withRel("get Article By Community"));
        resource.add(AccountController.getDOSCURL("/docs/index.html#resources-Article-Comments-create"));

        return new ResponseEntity(resource, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/article/{articleId}/comments/{commentsId}")
    @Transactional
    public ResponseEntity updateComment(@CurrentAccount AccountVO accountVO, @PathVariable("id") Long communityId,
                                      @PathVariable("articleId") Long articleId, @PathVariable("commentsId") Long commentId,
                                      @RequestBody @Valid CommentForm commentForm, Errors errors) {
        if (this.checkAccountVO(accountVO)) return this.returnFORBIDDEN();
        if (this.checkErrors(errors)) return this.returnBadRequestWithErrors(errors);

        Optional<Community> communityRepositoryById = this.communityRepository.findById(communityId);
        Optional<Article> articleRepositoryById = this.articleRepository.findById(articleId);
        Optional<Comments> commentsRepositoryById = this.commentsRepository.findById(commentId);
        if (this.checkId(communityRepositoryById) || this.checkId(articleRepositoryById) || this.checkId(commentsRepositoryById)) {
            return this.returnNotFound();
        }

        Article article = articleRepositoryById.get();
        Community community = communityRepositoryById.get();
        if (!article.getCommunity().equals(community)) return this.returnBadRequest();

        this.commentsService.updateComments(commentForm, commentsRepositoryById.get());
        CommentsDTOResource resource = new CommentsDTOResource();
        resource.add(linkTo(AccountController.class).slash(article.getAccount().getId()).withRel("self"));
        resource.add(linkTo(CommunityController.class).slash(communityId).withRel("Community Site"));
        resource.add(linkTo(CommunityController.class).slash("/" + communityId + "/article/" + articleId)
                .withRel("get Article By Community"));
        resource.add(AccountController.getDOSCURL("/docs/index.html#resources-Article-Comments-update"));

        return new ResponseEntity(resource, HttpStatus.OK);
    }

    @DeleteMapping("/{id}/article/{articleId}/comments/{commentsId}")
    @Transactional
    public ResponseEntity deleteComment(@CurrentAccount AccountVO accountVO, @PathVariable("id") Long communityId,
                                        @PathVariable("articleId") Long articleId,
                                        @PathVariable("commentsId") Long commentId) {
        if (this.checkAccountVO(accountVO)) return this.returnFORBIDDEN();

        Optional<Community> communityRepositoryById = this.communityRepository.findById(communityId);
        Optional<Article> articleRepositoryById = this.articleRepository.findById(articleId);
        Optional<Comments> commentsRepositoryById = this.commentsRepository.findById(commentId);
        if (this.checkId(communityRepositoryById) || this.checkId(articleRepositoryById) || this.checkId(commentsRepositoryById)) {
            return this.returnNotFound();
        }

        Article article = articleRepositoryById.get();
        Community community = communityRepositoryById.get();
        if (!article.getCommunity().equals(community)) return this.returnBadRequest();

        this.commentsService.deleteComments(article, this.accountRepository.findByEmail(accountVO.getEmail()).get()
                , commentsRepositoryById.get());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(linkTo(CommunityController.class)
                .slash("/" + commentId + "/article/" + articleId).withRel("get Article").toUri());

        return this.returnNOCONTENT(httpHeaders);
    }

}
