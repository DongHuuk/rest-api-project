package org.kuroneko.restapiproject.account;

import org.kuroneko.restapiproject.account.domain.AccountForm;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

public class AccountFormConverter {

    public static class StringToEventConverter implements Converter<String, AccountForm> {
        @Override
        public AccountForm convert(String s) {
            AccountForm accountForm = new AccountForm();
            String[] split = s.split("\"");
            accountForm.setUsername(split[3]);
            accountForm.setEmail(split[7]);
            accountForm.setPassword(split[11]);
            accountForm.setCheckingPassword(split[15]);
            return accountForm;
        }
    }
}
