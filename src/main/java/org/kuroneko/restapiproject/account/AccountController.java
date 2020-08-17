package org.kuroneko.restapiproject.account;

import org.kuroneko.restapiproject.account.validation.AccountValidation;
import org.kuroneko.restapiproject.domain.Account;
import org.kuroneko.restapiproject.domain.AccountForm;
import org.kuroneko.restapiproject.errors.ErrorsResource;
import org.kuroneko.restapiproject.main.MainController;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;

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
        WebMvcLinkBuilder selfLink = WebMvcLinkBuilder.linkTo(AccountController.class).slash(account.getId());
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
    public ResponseEntity findAccountsArticles(@CurrentAccount Account account, @PathVariable("id") Long id) {
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        if (!account.getId().equals(id)) {
            return ResponseEntity.badRequest().build();
        }

        Account accountWithArticles = accountRepository.findAccountWithArticleById(id);

        AccountResource accountResource = new AccountResource(accountWithArticles);
        return ResponseEntity.ok(accountResource);
    }

    //checked 방식을 어떻게 할것인가. Ajax로 checked된 값을 ","로 구분하여 JSON으로 전송
    @DeleteMapping("/{id}/articles")
    public ResponseEntity deleteAccountsArticles(@CurrentAccount Account account, @PathVariable("id") Long id, @RequestBody String checked) {
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        if (!account.getId().equals(id)) {
            return ResponseEntity.badRequest().build();
        }

        Account accountWithArticles = accountRepository.findAccountWithArticleById(id);

        AccountResource accountResource = new AccountResource(accountWithArticles);
        //TODO append CreateArticle Link
        HttpHeaders headers = new HttpHeaders();
        URI uri = WebMvcLinkBuilder.linkTo(AccountController.class).slash(account.getId() + "/articles").toUri();
        headers.setLocation(uri);

        if (checked == null) {
            return new ResponseEntity(headers, HttpStatus.SEE_OTHER);
        }

        accountService.findArticlesAndDelete(accountWithArticles, checked);

        return new ResponseEntity(accountResource, headers, HttpStatus.SEE_OTHER);
    }



}
