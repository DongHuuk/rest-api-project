package org.kuroneko.restapiproject.account;

import com.google.common.base.Ascii;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kuroneko.restapiproject.domain.Account;
import org.kuroneko.restapiproject.domain.AccountForm;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountServiceTest {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AccountService accountService;

    private AccountForm createAccountForm(String email, String username){
        AccountForm accountForm = new AccountForm();
        accountForm.setEmail(email);
        accountForm.setPassword("12341234");
        accountForm.setCheckingPassword("12341234");
        accountForm.setUsername(username);
        return accountForm;
    }

    private Account saveAccount(AccountForm accountForm) {
        Account account = modelMapper.map(accountForm, Account.class);
        return accountService.createNewAccount(account);
    }

    @BeforeEach
    private void createAccountBy30() {
        int count = 0;
        while (true) {
            if (count == 30) {
                break;
            }
            AccountForm accountForm = createAccountForm(UUID.randomUUID().toString(), "테스트" + count);
            saveAccount(accountForm);

            count ++;
        }
    }

    @AfterEach
    private void deleteRepository() {
        this.accountRepository.deleteAll();
    }

    @Test
    @DisplayName("Filter Test")
    public void checkUpdateAccount_Test(){
        List<Account> all = accountRepository.findAll();
        List<Long> collect = new ArrayList<>();
        Account account = all.get(3);
        System.out.println(account.getUsername());
        account.setUsername("테스트15");

        List<String> filter = all.stream()
                .map(Account::getUsername)
                .filter(a -> a.equals("테스트15"))
                .collect(Collectors.toList());

        System.out.println(filter.isEmpty());

        if (!filter.isEmpty()) {
            List<Account> accountList = accountRepository.findAllByUsername("테스트15");
            collect = accountList.stream().map(Account::getId).filter(id -> !account.getId().equals(id)).collect(Collectors.toList());
            // !collect.isEmpty() => "테스트15"는 사용 못한다.
        }
    }

    @Test
    public void test(){
        char a = 'a';
        System.out.println(a + "");
        System.out.println("-=--0=-=-=-=");
    }
    
}