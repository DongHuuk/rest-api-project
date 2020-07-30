package org.kuroneko.restapiproject.account;

import org.kuroneko.restapiproject.account.validation.AccountValidation;
import org.kuroneko.restapiproject.domain.Account;
import org.kuroneko.restapiproject.domain.AccountForm;
import org.kuroneko.restapiproject.errors.ErrorsResource;
import org.kuroneko.restapiproject.main.MainController;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping(value = "/accounts", produces = "application/hal+json;charset=UTF-8")
public class AccountController {

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountValidation accountValidation;
    @Autowired
    private AccountRepository accountRepository;

    @InitBinder
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

}
