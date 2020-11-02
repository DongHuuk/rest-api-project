package org.kuroneko.restapiproject.account;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.kuroneko.restapiproject.account.domain.Account;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class AccountResource extends EntityModel<Account> {
    public AccountResource(Account account, Link... links) {
        super(account, links);
        add(WebMvcLinkBuilder.linkTo(AccountController.class).slash(account.getId()).withSelfRel());
    }
}
