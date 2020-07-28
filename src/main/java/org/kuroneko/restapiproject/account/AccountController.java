package org.kuroneko.restapiproject.account;

import org.kuroneko.restapiproject.account.validation.AccountValidation;
import org.kuroneko.restapiproject.domain.Account;
import org.kuroneko.restapiproject.domain.AccountForm;
import org.kuroneko.restapiproject.errors.ErrorsResource;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

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
            return ResponseEntity.badRequest().body(new ErrorsResource(errors));
        }

        errors = accountService.checkAccountEmail(accountForm, errors);

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorsResource(errors));
        }

        Account account = accountService.createNewAccount(modelMapper.map(accountForm, Account.class));
        WebMvcLinkBuilder selfLink = WebMvcLinkBuilder.linkTo(AccountController.class).slash(account.getId());
        AccountResource accountResource = new AccountResource(account);

        return ResponseEntity.created(selfLink.toUri()).body(accountResource);
    }

    @PutMapping("/{id}")
    public ResponseEntity updateAccount(@RequestBody @Valid AccountForm accountForm, Errors errors, @PathVariable("id") String accountId) {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }
        Optional<Account> accountById = accountRepository.findById(Long.parseLong(accountId));

        if (accountById.isEmpty()) {
            errors.rejectValue("id", "wrong.id", "not find id");
            return ResponseEntity.badRequest().body(errors);
        }
        Account account = accountById.get();
        modelMapper.map(accountForm, account);

        Account newAccount = accountService.updateAccount(account);

        return ResponseEntity.ok(newAccount);
    }

}
