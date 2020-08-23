package org.kuroneko.restapiproject.account;

import javassist.NotFoundException;
import org.kuroneko.restapiproject.account.validation.AccountValidation;
import org.kuroneko.restapiproject.article.ArticleDTO;
import org.kuroneko.restapiproject.article.ArticleRepository;
import org.kuroneko.restapiproject.comments.CommentsDTO;
import org.kuroneko.restapiproject.domain.Account;
import org.kuroneko.restapiproject.domain.AccountForm;
import org.kuroneko.restapiproject.domain.Comments;
import org.kuroneko.restapiproject.domain.Notification;
import org.kuroneko.restapiproject.errors.ErrorsResource;
import org.kuroneko.restapiproject.main.MainController;
import org.kuroneko.restapiproject.notification.NotificationDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.web.servlet.headers.HttpStrictTransportSecurityDsl;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

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
    private AccountRepository accountRepository;
    @Autowired
    private ArticleRepository articleRepository;

    @InitBinder("accountForm")
    public void checkingAccountForm(WebDataBinder webDataBinder){
        webDataBinder.addValidators(accountValidation);
    }

    //Json
    @PostMapping
    public ResponseEntity createAccount(@RequestBody @Valid AccountForm accountForm, Errors errors){
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        errors = accountService.checkAccountEmailAndUsername(accountForm, errors);

        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        Account account = accountService.createNewAccount(modelMapper.map(accountForm, Account.class));
        WebMvcLinkBuilder selfLink = linkTo(AccountController.class).slash(account.getId());
        AccountResource accountResource = new AccountResource(account);

        return ResponseEntity.created(selfLink.toUri()).body(accountResource);
    }

    //Account Profile을 보여주며 수정할 수 있는 form 또한 제공이 되어야 한다. (F)
    @GetMapping("/{id}")
    public ResponseEntity accountProfile(@PathVariable Long id, @CurrentAccount Account account) {


        return null;
    }

    @PutMapping("/{id}")
    public ResponseEntity updateAccount(@PathVariable Long id, @RequestBody @Valid AccountForm accountForm, Errors errors) {
        if (errors.hasErrors()) {
            return badRequest(errors);
        }
        Optional<Account> accountById = accountRepository.findById(id);

        if (accountById.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Account account = accountById.get();

        errors = accountService.checkUpdateAccount(accountForm, errors, account);

        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        modelMapper.map(accountForm, account);

        Account newAccount = accountService.updateAccount(account);
        AccountResource accountResource = new AccountResource(newAccount);

        return ResponseEntity.ok(accountResource);
    }

    private ResponseEntity<ErrorsResource> badRequest(Errors errors) {
        return ResponseEntity.badRequest().body(new ErrorsResource(errors));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteAccount(@PathVariable("id") Long id, @RequestBody @Valid AccountForm accountForm, Errors errors) {
        if (errors.hasErrors()) {
            return this.badRequest(errors);
        }
        Optional<Account> byId = this.accountRepository.findById(id);

        if (byId.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        this.accountService.deleteAccount(byId.get());

        return ResponseEntity.ok().body(MainController.getIndexLink());
    }

    //게시글 리턴
    @GetMapping("/{id}/articles")
    public ResponseEntity findAccountsArticles(@CurrentAccount Account account, @PathVariable("id") Long id
                                            ,@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable) {
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        if (!account.getId().equals(id)) {
            return ResponseEntity.badRequest().build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(linkTo(AccountController.class).slash(account.getId()).toUri());
        Page<ArticleDTO> articleDTO = accountService.createPageableArticle(id, pageable, account);

        return new ResponseEntity<Object>(articleDTO, headers, HttpStatus.OK);
    }

    //checked 방식을 어떻게 할것인가. Ajax로 checked된 값을 ","로 구분하여 JSON으로 전송
    @DeleteMapping("/{id}/articles")
    public ResponseEntity deleteAccountsArticles(@CurrentAccount Account account, @PathVariable("id") Long id,
                                                 @RequestBody String checked, Errors errors) {
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        if (!account.getId().equals(id)) {
            return ResponseEntity.badRequest().build();
        }

        Account accountWithArticle = accountRepository.findAccountWithArticleById(id);
        AccountResource accountResource = new AccountResource(accountWithArticle);
        accountResource.add(linkTo(AccountController.class).slash(account.getId() + "/articles").withRel("getArticles"));
        //TODO append link - create articles

        if (checked == null) {
            return new ResponseEntity(accountResource, HttpStatus.SEE_OTHER);
        }

        try {
            accountService.findArticlesAndDelete(accountWithArticle, checked);
        } catch (NotFoundException e) {
            errors.rejectValue("number", "wrong.number", "not found articles by numbers");
            ErrorsResource errorsResource = new ErrorsResource(errors);
            return new ResponseEntity(errorsResource, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity(accountResource, HttpStatus.SEE_OTHER);
    }

    //댓글들 리턴
    @GetMapping("/{id}/comments")
    public ResponseEntity findAccountsComments(@CurrentAccount Account account, @PathVariable("id") Long id,
                                               @PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable) {
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        if (!account.getId().equals(id)) {
            return ResponseEntity.badRequest().build();
        }

        Page<CommentsDTO> commentsDTO = accountService.createPageableComments(id, pageable, account);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(linkTo(AccountController.class).slash(account.getId()).toUri());

        return new ResponseEntity<Object>(commentsDTO, headers, HttpStatus.OK);
    }

    //checked 방식은 게시글과 동일
    @DeleteMapping("/{id}/comments")
    public ResponseEntity deleteAccountsComments(@CurrentAccount Account account, @PathVariable("id") Long id,
                                                 @RequestBody String checked, Errors errors) {
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        if (!account.getId().equals(id)) {
            return ResponseEntity.badRequest().build();
        }

        Account accountWithComments = accountRepository.findAccountWithCommentsById(id);
        AccountResource accountResource = new AccountResource(accountWithComments);
        accountResource.add(linkTo(AccountController.class).slash(account.getId() + "/comments").withRel("getComments"));

        if (checked == null) {
            return new ResponseEntity(accountResource, HttpStatus.SEE_OTHER);
        }

        try {
            accountService.findCommentsAndDelete(accountWithComments, checked);
        } catch (NotFoundException e) {
            errors.rejectValue("number", "wrong.number", "not found comments by numbers");
            ErrorsResource errorsResource = new ErrorsResource(errors);
            return new ResponseEntity(errorsResource, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity(accountResource, HttpStatus.SEE_OTHER);
    }

    //알림들 리턴
    @GetMapping("/{id}/notification")
    public ResponseEntity findAccountsNotifications(@CurrentAccount Account account, @PathVariable("id") Long id,
                                                    @PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable) {
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        if (!account.getId().equals(id)) {
            return ResponseEntity.badRequest().build();
        }

        Page<NotificationDTO> pageableNotification = accountService.createPageableNotification(id, pageable, account);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(linkTo(AccountController.class).slash(account.getId()).toUri());

        return new ResponseEntity(pageableNotification, httpHeaders, HttpStatus.OK);
    }

    //다른 article과 commnets와 동일하게 동작
    @DeleteMapping("/{id}/notification")
    public ResponseEntity deleteAccountsNotifications(@CurrentAccount Account account, @PathVariable("id") Long id, @RequestBody String checked, Errors errors) {
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        if (!account.getId().equals(id)) {
            return ResponseEntity.badRequest().build();
        }

        Account accountWithNotification = accountRepository.findAccountWithNotificationById(id);
        AccountResource accountResource = new AccountResource(accountWithNotification);
        accountResource.add(linkTo(AccountController.class).slash(account.getId() + "/notification").withRel("getNotification"));

        try {
            accountService.findNotificationAndDelete(accountWithNotification, checked);
        } catch (NotFoundException e) {
            errors.rejectValue("number", "wrong.number", "not found comments by numbers");
            ErrorsResource errorsResource = new ErrorsResource(errors);
            return new ResponseEntity(errorsResource, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity(accountResource, HttpStatus.SEE_OTHER);
    }

}
