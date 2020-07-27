package org.kuroneko.restapiproject.account;

import lombok.NoArgsConstructor;
import org.kuroneko.restapiproject.domain.Account;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

@NoArgsConstructor
public class AccountResource extends EntityModel<Account> {
    public AccountResource(Account account, Link... links) {
        super(account, links);
        add(WebMvcLinkBuilder.linkTo(AccountController.class).slash(account.getId()).withSelfRel());
    }
}
