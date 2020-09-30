package org.kuroneko.restapiproject.account;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.kuroneko.restapiproject.account.validation.AccountPasswordValidation;
import org.kuroneko.restapiproject.account.validation.AccountValidation;
import org.kuroneko.restapiproject.article.ArticleDTO;
import org.kuroneko.restapiproject.comments.CommentsDTO;
import org.kuroneko.restapiproject.domain.Account;
import org.kuroneko.restapiproject.domain.AccountForm;
import org.kuroneko.restapiproject.domain.AccountPasswordForm;
import org.kuroneko.restapiproject.errors.ErrorsResource;
import org.kuroneko.restapiproject.main.MainController;
import org.kuroneko.restapiproject.notification.NotificationDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
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

    @PostMapping("/create")
    public ResponseEntity createAccount(@RequestBody @Valid AccountForm accountForm, Errors errors){
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        errors = accountService.checkAccountEmailAndUsername(accountForm, errors);

        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        Account account = accountService.createNewAccount(modelMapper.map(accountForm, Account.class));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(linkTo(AccountController.class).slash(account.getId()).toUri());

        return new ResponseEntity(httpHeaders, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity sendAccount(@PathVariable Long id, @CurrentAccount Account account) {
        if (account == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<Account> byId = this.accountRepository.findById(id);

        if (byId.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Account newAccount = byId.get();
        AccountResource accountResource = new AccountResource(newAccount);
        accountResource.add(linkTo(MainController.class).withRel("main"));
        accountResource.add(linkTo(AccountController.class).slash(newAccount.getId() + "/articles").withRel("accounts Articles"));
        accountResource.add(linkTo(AccountController.class).slash(newAccount.getId() + "/comments").withRel("accounts Comments"));

        return new ResponseEntity(accountResource, HttpStatus.OK);
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

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(linkTo(AccountController.class).slash(account.getId()).toUri());

        return new ResponseEntity(httpHeaders, HttpStatus.SEE_OTHER);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteAccount(@PathVariable("id") Long id, @RequestBody @Valid AccountPasswordForm accountPasswordForm, Errors errors) {
        if (errors.hasErrors()) {
            return this.badRequest(errors);
        }
        Optional<Account> byId = this.accountRepository.findById(id);

        if (byId.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        this.accountService.deleteAccount(byId.get());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(linkTo(MainController.class).toUri());

        return new ResponseEntity(httpHeaders, HttpStatus.SEE_OTHER);
    }

    //embeded로 한번 wrapping되는데 모르겟음
    @GetMapping("/{id}/articles")
    public ResponseEntity findAccountsArticles(@CurrentAccount Account account, @PathVariable("id") Long id
                                            , @PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable,
                                               PagedResourcesAssembler<ArticleDTO> assembler) {
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        if (!account.getId().equals(id)) {
            return ResponseEntity.badRequest().build();
        }

        Page<ArticleDTO> articleDTO = accountService.createPageableArticle(id, pageable, account);

        return new ResponseEntity(assembler.toModel(articleDTO,
                linkTo(AccountController.class).slash(id + "/articles").withRel("get articles")),
                HttpStatus.OK);
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
        Link link = linkTo(AccountController.class).slash(account.getId() + "/articles").withRel("getArticles");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(link.toUri());

        if (checked == null) {
            return new ResponseEntity(httpHeaders, HttpStatus.SEE_OTHER);
        }

        try {
            accountService.findArticlesAndDelete(accountWithArticle, checked);
        } catch (NotFoundException e) {
            errors.rejectValue("number", "wrong.number", "not found articles by numbers");
            ErrorsResource errorsResource = new ErrorsResource(errors);
            return new ResponseEntity(errorsResource, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity(httpHeaders, HttpStatus.SEE_OTHER);
    }

    //댓글들 리턴
    @GetMapping("/{id}/comments")
    public ResponseEntity findAccountsComments(@CurrentAccount Account account, @PathVariable("id") Long id,
                                               @PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable,
                                               PagedResourcesAssembler<CommentsDTO> assembler) {
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        if (!account.getId().equals(id)) {
            return ResponseEntity.badRequest().build();
        }

        Page<CommentsDTO> commentsDTO = accountService.createPageableComments(id, pageable, account);

        return new ResponseEntity(assembler.toModel(commentsDTO,
                linkTo(AccountController.class).slash(id + "/comments").withRel("get Comments")),
                HttpStatus.OK);
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

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(linkTo(AccountController.class).slash(account.getId() + "/comments").withRel("getComments").toUri());

        if (checked == null) {
            return new ResponseEntity(httpHeaders, HttpStatus.SEE_OTHER);
        }

        try {
            accountService.findCommentsAndDelete(accountWithComments, checked);
        } catch (NotFoundException e) {
            errors.rejectValue("number", "wrong.number", "not found comments by numbers");
            ErrorsResource errorsResource = new ErrorsResource(errors);
            return new ResponseEntity(errorsResource, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity(httpHeaders, HttpStatus.SEE_OTHER);
    }

    //알림들 리턴
    @GetMapping("/{id}/notification")
    public ResponseEntity findAccountsNotifications(@CurrentAccount Account account, @PathVariable("id") Long id,
                                                    @PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable,
                                                    PagedResourcesAssembler<NotificationDTO> assembler) {
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        if (!account.getId().equals(id)) {
            return ResponseEntity.badRequest().build();
        }

        Page<NotificationDTO> pageableNotification = accountService.createPageableNotification(id, pageable, account);

        return new ResponseEntity(assembler.toModel(pageableNotification,
                linkTo(AccountController.class).slash(account.getId() + "/notification").withRel("get Notification")),
                HttpStatus.OK);
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

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(linkTo(AccountController.class).slash(account.getId() + "/notification")
                                .withRel("get Notification").toUri());

        if (checked == null) {
            return new ResponseEntity(httpHeaders, HttpStatus.SEE_OTHER);
        }

        try {
            accountService.findNotificationAndDelete(accountWithNotification, checked);
        } catch (NotFoundException e) {
            errors.rejectValue("number", "wrong.number", "not found comments by numbers");
            ErrorsResource errorsResource = new ErrorsResource(errors);
            return new ResponseEntity(errorsResource, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity(httpHeaders, HttpStatus.SEE_OTHER);
    }

}
