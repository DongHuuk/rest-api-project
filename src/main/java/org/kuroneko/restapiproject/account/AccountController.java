package org.kuroneko.restapiproject.account;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.account.domain.AccountForm;
import org.kuroneko.restapiproject.account.domain.AccountPasswordForm;
import org.kuroneko.restapiproject.account.validation.AccountPasswordValidation;
import org.kuroneko.restapiproject.account.validation.AccountValidation;
import org.kuroneko.restapiproject.article.domain.ArticleDTO;
import org.kuroneko.restapiproject.comments.CommentsDTO;
import org.kuroneko.restapiproject.errors.ErrorsResource;
import org.kuroneko.restapiproject.main.MainController;
import org.kuroneko.restapiproject.notification.NotificationDTO;
import org.kuroneko.restapiproject.token.AccountVO;
import org.kuroneko.restapiproject.token.CurrentAccount;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Slf4j
@RestController
@RequestMapping(value = "/accounts", produces = "application/hal+json;charset=UTF-8")
public class AccountController {
    //TODO 매핑 객체 미완성으로 인해 link는 self만 추가하였음. 메서드가 완성 되었다면 추가적으로 입력해야 할
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountValidation accountValidation;
    @Autowired
    private AccountPasswordValidation accountPasswordValidation;
    @Autowired
    private AccountRepository accountRepository;

    @Value("${host}")
    private String host;

    @InitBinder("accountForm")
    public void checkingAccountForm(WebDataBinder webDataBinder){
        webDataBinder.addValidators(accountValidation);
    }
    @InitBinder("accountPasswordForm")
    public void checkingAccountPasswordForm(WebDataBinder webDataBinder){
        webDataBinder.addValidators(accountPasswordValidation);
    }

    private ResponseEntity<ErrorsResource> badRequest(Errors errors) {
        return ResponseEntity.badRequest().body(new ErrorsResource(errors));
    }

    private Link getAccountProfile(Long id){
        return linkTo(AccountController.class).slash(id).withRel("Account Profile").withType("JSON");
    }

    private Link getAccountArticles(Long id){
        return linkTo(AccountController.class).slash(id + "/articles").withRel("get Articles").withType("JSON");
    }

    private Link getAccountComments(Long id){
        return linkTo(AccountController.class).slash(id + "/comments").withRel("get Comments").withType("JSON");
    }

    private Link getAccountNotification(Long id){
        return linkTo(AccountController.class).slash(id + "/notification").withRel("get Notification").withType("JSON");
    }

    private Link getDOSCURL(String url){
        return Link.of("http://" + host + url).withRel("DOCS").withType("JSON");
    }

    private AccountResource createAccountResource(Account account, Long id){
        AccountResource accountResource = new AccountResource(account);
        accountResource.add(this.getAccountProfile(id));
        accountResource.add(this.getAccountArticles(id));
        accountResource.add(this.getAccountComments(id));
        accountResource.add(this.getAccountNotification(id));
        return accountResource;
    }

    private AccountResource createAccountResource(Long id){
        AccountResource accountResource = new AccountResource();
        accountResource.add(this.getAccountProfile(id));
        accountResource.add(this.getAccountArticles(id));
        accountResource.add(this.getAccountComments(id));
        accountResource.add(this.getAccountNotification(id));
        return accountResource;
    }

    private boolean checkAccountVO(AccountVO accountVO) {
        return accountVO == null;
    }
    private ResponseEntity returnFORBIDDEN(){
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }

    private boolean checkErrors(Errors errors){
        return errors.hasErrors();
    }

    private boolean checkId(Optional<?> objectOptional) {
        return objectOptional.isEmpty();
    }

    private ResponseEntity returnNotFound(){
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }

    private boolean checkEmail(String accountEmail, String accountVOEmail) {
        return accountEmail.equals(accountVOEmail);
    }

    private ResponseEntity returnBadRequest(){
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity returnNOCONTENT(HttpHeaders httpHeaders) {
        return new ResponseEntity(httpHeaders, HttpStatus.NO_CONTENT);
    }

    @PostMapping
    public ResponseEntity createAccount(@RequestBody @Valid AccountForm accountForm, Errors errors){
        if (this.checkErrors(errors)) return badRequest(errors);

        errors = accountService.checkAccountEmailAndUsername(accountForm, errors);

        if (this.checkErrors(errors)) return badRequest(errors);

        Account newAccount = accountService.createNewAccount(modelMapper.map(accountForm, Account.class));

        AccountResource accountResource = this.createAccountResource(newAccount.getId());
        accountResource.add(this.getDOSCURL("/docs/index.html#resources-account-create"));

        return new ResponseEntity(accountResource, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity findAccount(@CurrentAccount AccountVO accountVO, @PathVariable Long id) {
        if (this.checkAccountVO(accountVO)) return this.returnFORBIDDEN();

        Optional<Account> byId = this.accountRepository.findById(id);

        if (this.checkId(byId)) return this.returnNotFound();

        Account newAccount = byId.get();
        AccountResource accountResource = this.createAccountResource(newAccount, newAccount.getId());
        accountResource.add(this.getDOSCURL("/docs/index.html#resources-account-get"));

        return new ResponseEntity(accountResource, HttpStatus.OK);
    }
    //accountForm에서 Email의 값은 입력불가(수정불가)

    @PutMapping("/{id}")
    public ResponseEntity updateAccount(@CurrentAccount AccountVO accountVO,
                                        @PathVariable Long id,
                                        @RequestBody @Valid AccountForm accountForm, Errors errors) {
        if (this.checkAccountVO(accountVO)) return this.returnFORBIDDEN();
        if (this.checkErrors(errors)) return this.badRequest(errors);

        Optional<Account> accountById = accountRepository.findById(id);
        if (this.checkId(accountById)) return this.returnNotFound();

        Account account = accountById.get();

        errors = accountService.checkUpdateAccount(accountForm, errors, account);
        if (this.checkErrors(errors)) return this.badRequest(errors);

        this.accountService.updateAccount(account, accountForm);

        AccountResource accountResource = this.createAccountResource(account.getId());
        accountResource.add(this.getDOSCURL("/docs/index.html#resources-account-update"));

        return new ResponseEntity(accountResource, HttpStatus.CREATED);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity deleteAccount(@CurrentAccount AccountVO accountVO,
                                        @PathVariable("id") Long id,
                                        @RequestBody @Valid AccountPasswordForm accountPasswordForm,
                                        Errors errors) {
        if(this.checkAccountVO(accountVO)) return this.returnFORBIDDEN();
        if (this.checkErrors(errors)) return this.badRequest(errors);
        Optional<Account> byId = this.accountRepository.findById(id);

        if (this.checkId(byId)) return this.returnNotFound();

        this.accountService.deleteAccount(byId.get(), accountPasswordForm);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(linkTo(MainController.class).toUri());

        return new ResponseEntity(httpHeaders, HttpStatus.NO_CONTENT);
    }
    //TODO 이 아래부터 작업 재개

    @GetMapping("/{id}/articles")
    public ResponseEntity findAccountsArticles(@CurrentAccount AccountVO accountVO, @PathVariable("id") Long id
                                            , @PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable,
                                               PagedResourcesAssembler<ArticleDTO> assembler) {
        if (this.checkAccountVO(accountVO)) return this.returnFORBIDDEN();

        Optional<Account> byId = this.accountRepository.findById(id);
        if(this.checkId(byId)) return this.returnNotFound();

        Account account = byId.get();
        if (!this.checkEmail(account.getEmail(), accountVO.getEmail())) return this.returnBadRequest();

        Page<ArticleDTO> articleDTO = accountService.createPageableArticle(id, pageable, account);
        PagedModel<EntityModel<ArticleDTO>> getArticles = assembler.toModel(articleDTO, this.getAccountProfile(id));
        getArticles.add(this.getAccountArticles(id));
        getArticles.add(this.getAccountComments(id));
        getArticles.add(this.getAccountNotification(id));
        getArticles.add(this.getDOSCURL("/docs/index.html#resources-Account-article-get"));

        return new ResponseEntity(getArticles, HttpStatus.OK);
    }

    //checked 방식을 어떻게 할것인가. Ajax로 checked된 값을 ","로 구분하여 JSON으로 전송
    @DeleteMapping("/{id}/articles")
    public ResponseEntity deleteAccountsArticles(@CurrentAccount AccountVO accountVO, @PathVariable("id") Long id,
                                                 @RequestBody String checked, Errors errors) {
        if (this.checkAccountVO(accountVO)) return this.returnFORBIDDEN();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(linkTo(AccountController.class).slash(id + "/articles").withRel("get Articles").toUri());

        Optional<Account> accountWithArticleById = accountRepository.findAccountWithArticleById(id);
        if(this.checkId(accountWithArticleById)) return this.returnNotFound();

        Account account = accountWithArticleById.get();
        if (!this.checkEmail(account.getEmail(), accountVO.getEmail())) return this.returnBadRequest();

        try {
            accountService.findArticlesAndDelete(account, checked);
        } catch (NotFoundException e) {
            errors.reject( "wrong.number", "not found articles by numbers");
            ErrorsResource errorsResource = new ErrorsResource(errors);
            return new ResponseEntity(errorsResource, HttpStatus.BAD_REQUEST);
        }

        return this.returnNOCONTENT(httpHeaders);
    }

    //TODO 여기부터
    //댓글들 리턴
    @GetMapping("/{id}/comments")
    public ResponseEntity findAccountsComments(@CurrentAccount AccountVO accountVO, @PathVariable("id") Long id,
                                               @PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable,
                                               PagedResourcesAssembler<CommentsDTO> assembler) {
        if (this.checkAccountVO(accountVO)) return this.returnFORBIDDEN();

        Optional<Account> byId = this.accountRepository.findById(id);
        if(this.checkId(byId)) return this.returnNotFound();

        Account account = byId.get();
        if (!this.checkEmail(account.getEmail(), accountVO.getEmail())) return this.returnBadRequest();

        Page<CommentsDTO> commentsDTO = accountService.createPageableComments(id, pageable, account);
        PagedModel<EntityModel<CommentsDTO>> getComments = assembler.toModel(commentsDTO, this.getAccountProfile(id));
        getComments.add(this.getAccountArticles(id));
        getComments.add(this.getAccountComments(id));
        getComments.add(this.getAccountNotification(id));
        getComments.add(this.getDOSCURL("/docs/index.html#resources-Account-comments-get"));

        return new ResponseEntity(getComments, HttpStatus.OK);
    }

    //checked 방식은 게시글과 동일
    @DeleteMapping("/{id}/comments")
    public ResponseEntity deleteAccountsComments(@CurrentAccount AccountVO accountVO, @PathVariable("id") Long id,
                                                 @RequestBody String checked, Errors errors) {
        if (this.checkAccountVO(accountVO)) return this.returnFORBIDDEN();

        Optional<Account> accountWithCommentsById = accountRepository.findAccountWithCommentsById(id);
        if (this.checkId(accountWithCommentsById)) return this.returnNotFound();

        Account account = accountWithCommentsById.get();
        if (!this.checkEmail(account.getEmail(), accountVO.getEmail())) return this.returnBadRequest();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(linkTo(AccountController.class).slash(id + "/comments")
                .withRel("get Comments").toUri());

        try {
            accountService.findCommentsAndDelete(account, checked);
        } catch (NotFoundException e) {
            errors.reject( "wrong.number", "not found comments by numbers");
            ErrorsResource errorsResource = new ErrorsResource(errors);
            return new ResponseEntity(errorsResource, HttpStatus.BAD_REQUEST);
        }

        return this.returnNOCONTENT(httpHeaders);
    }

    //알림들 리턴
    @GetMapping("/{id}/notification")
    public ResponseEntity findAccountsNotifications(@CurrentAccount AccountVO accountVO, @PathVariable("id") Long id,
                                                    @PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable,
                                                    PagedResourcesAssembler<NotificationDTO> assembler) {
        if (this.checkAccountVO(accountVO)) return this.returnFORBIDDEN();

        Optional<Account> byId = this.accountRepository.findById(id);
        if (this.checkId(byId)) return this.returnNotFound();

        Account account = byId.get();
        if (!this.checkEmail(account.getEmail(), accountVO.getEmail())) return this.returnBadRequest();

        Page<NotificationDTO> pageableNotification = accountService.createPageableNotification(id, pageable, account);
        PagedModel<EntityModel<NotificationDTO>> getNotification = assembler.toModel(pageableNotification, this.getAccountProfile(id));
        getNotification.add(this.getAccountArticles(id));
        getNotification.add(this.getAccountComments(id));
        getNotification.add(this.getAccountNotification(id));
        getNotification.add(this.getDOSCURL("/docs/index.html#resources-Account-notification-get"));

        return new ResponseEntity(getNotification, HttpStatus.OK);
    }

    //다른 article과 commnets와 동일하게 동작
    @DeleteMapping("/{id}/notification")
    public ResponseEntity deleteAccountsNotifications(@CurrentAccount AccountVO accountVO, @PathVariable("id") Long id, @RequestBody String checked, Errors errors) {
        if (this.checkAccountVO(accountVO)) return this.returnFORBIDDEN();

        Optional<Account> accountWithNotificationById = accountRepository.findAccountWithNotificationById(id);
        if (this.checkId(accountWithNotificationById)) return this.returnNotFound();

        Account account = accountWithNotificationById.get();
        if (!this.checkEmail(account.getEmail(), accountVO.getEmail())) return this.returnBadRequest();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(linkTo(AccountController.class).slash(id + "/notification")
                                .withRel("get Notification").toUri());

        try {
            accountService.findNotificationAndDelete(account, checked);
        } catch (NotFoundException e) {
            errors.rejectValue("number", "wrong.number", "not found comments by numbers");
            ErrorsResource errorsResource = new ErrorsResource(errors);
            return new ResponseEntity(errorsResource, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity(httpHeaders, HttpStatus.NO_CONTENT);
    }
}
