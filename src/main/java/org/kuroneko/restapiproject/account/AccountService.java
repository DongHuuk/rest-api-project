package org.kuroneko.restapiproject.account;

import org.kuroneko.restapiproject.article.ArticleRepository;
import org.kuroneko.restapiproject.comments.CommentsRepository;
import org.kuroneko.restapiproject.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private CommentsRepository commentsRepository;

    public Account createNewAccount(Account account) {
        account.setCreateTime(LocalDateTime.now());
        account.setAuthority(UserAuthority.USER);
        account.setPassword(passwordEncoder.encode(account.getPassword()));

        return accountRepository.save(account);
    }

    public Errors checkAccountEmailAndUsername(AccountForm accountForm, Errors errors) {
        Optional<Account> byEmail = accountRepository.findByEmail(accountForm.getEmail());
        String username = accountForm.getUsername();

        List<String> filter = accountRepository.findAll().stream()
                .map(Account::getUsername)
                .filter(a -> a.equals(username))
                .collect(Collectors.toList());

        if (!filter.isEmpty()) {
            errors.rejectValue("username", "wrong.username", "duplicate username. check please");
        }

        if (byEmail.isPresent()) {
            errors.rejectValue("email", "wrong.email", "duplicate Email. check please");
        }
        return errors;
    }

    public Errors checkUpdateAccount(AccountForm accountForm, Errors errors, Account account) {
        List<Account> all = this.accountRepository.findAll();
        List<Long> collect = new ArrayList<>();
        String username = accountForm.getUsername();

        List<String> filter = all.stream()
                .map(Account::getUsername)
                .filter(a -> a.equals(username))
                .collect(Collectors.toList());

        if (!filter.isEmpty()) {
            List<Account> accountList = accountRepository.findAllByUsername(username);
            collect = accountList.stream().map(Account::getId).filter(id -> !account.getId().equals(id)).collect(Collectors.toList());
            // !collect.isEmpty() => "테스트15"는 사용 못한다.
        }

        if (!collect.isEmpty()) {
            errors.rejectValue("username", "wrong.username", "duplicate username. check please");
        }

        if (!this.passwordEncoder.matches(accountForm.getPassword(), account.getPassword())) {
            errors.rejectValue("password", "wrong.password", "unMatch Password. check please");
        }

        return errors;
    }

    public Account updateAccount(Account account) {
        account.setUpdateTime(LocalDateTime.now());
        return accountRepository.save(account);
    }

    public void deleteAccount(Account account) {
        this.accountRepository.delete(account);
    }

    public void findArticlesAndDelete(Account accountWithArticles, String checked) {
        String[] splitStr = checked.split(",");

        for (String str : splitStr) {
            str = str.trim();
            Optional<Article> byNumber = this.articleRepository.findByNumber(Long.valueOf(str));

            if (byNumber.isEmpty()) {
                //TODO Exception 처리
            }

            if (accountWithArticles.getArticle().contains(byNumber.get())) {
                accountWithArticles.getArticle().remove(byNumber.get());
                this.articleRepository.delete(byNumber.get());
            }
        }
    }

    public void findCommentsAndDelete(Account accountWithComments, String checked) {
        String[] splitStr = checked.split(",");

        for (String str : splitStr) {
            str = str.trim();
            Optional<Comments> byNumber = this.commentsRepository.findByNumber(Long.valueOf(str));

            if (byNumber.isEmpty()) {
                //TODO Exception 처리
            }

            if (accountWithComments.getComments().contains(byNumber.get())) {
                accountWithComments.getComments().remove(byNumber.get());
                this.commentsRepository.delete(byNumber.get());
            }
        }
    }
}
