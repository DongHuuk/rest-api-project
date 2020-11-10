package org.kuroneko.restapiproject.account;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.account.domain.AccountForm;
import org.kuroneko.restapiproject.account.domain.AccountPasswordForm;
import org.kuroneko.restapiproject.account.domain.UserAuthority;
import org.kuroneko.restapiproject.article.domain.Article;
import org.kuroneko.restapiproject.article.domain.ArticleDTO;
import org.kuroneko.restapiproject.comments.CommentsDTO;
import org.kuroneko.restapiproject.comments.CommentsRepository;
import org.kuroneko.restapiproject.comments.domain.Comments;
import org.kuroneko.restapiproject.exception.PasswordInputError;
import org.kuroneko.restapiproject.notification.NotificationDTO;
import org.kuroneko.restapiproject.notification.NotificationRepository;
import org.kuroneko.restapiproject.notification.domain.Notification;
import org.kuroneko.restapiproject.token.AccountVO;
import org.kuroneko.restapiproject.token.AccountVORepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private org.kuroneko.restapiproject.article.ArticleRepository articleRepository;
    @Autowired
    private CommentsRepository commentsRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private AccountVORepository accountVORepository;

    public Account createNewAccount(Account account) {
        account.setCreateTime(LocalDateTime.now());
        account.setAuthority(UserAuthority.USER);
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        AccountVO accountVO = new AccountVO(account.getEmail(), account.getPassword(), account.getAuthority());
        accountVO.setCreatedAt(LocalDateTime.now());
        accountVO.setAuthority(UserAuthority.USER);
        this.accountVORepository.save(accountVO);

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

//        if (!this.passwordEncoder.matches(accountForm.getPassword(), account.getPassword())) {
//            errors.rejectValue("password", "wrong.password", "unMatch Password. check please");
//        }

        return errors;
    }

    public Account updateAccount(Account account, AccountForm accountForm) {
        account.setUpdateTime(LocalDateTime.now());
        account.setUsername(accountForm.getUsername());
        account.setPassword(this.passwordEncoder.encode(accountForm.getPassword()));

        return accountRepository.save(account);
    }

    public void deleteAccount(Account account, AccountPasswordForm accountPasswordForm) {
        if (!accountPasswordForm.checkedPassword()) throw new PasswordInputError();
        if (!this.passwordEncoder.matches(accountPasswordForm.getPassword(), account.getPassword()))
            throw new PasswordInputError();
        this.accountRepository.delete(account);
    }

    public void findArticlesAndDelete(Account accountWithArticles, String checked) throws NotFoundException {
        String[] split = checked.split(",");
        List<Long> collect = Arrays.stream(split).map(s -> {
            s = s.trim();
            return Long.valueOf(s);
        }).collect(Collectors.toList());

        List<Article> byNumber = this.articleRepository.findByNumber(collect);

        if (byNumber.isEmpty()) {
            throw new NotFoundException("Not Found Articles by Numbers");
        }

        for (Article  article : byNumber) {
            if (accountWithArticles.getArticle().contains(article)) {
                accountWithArticles.getArticle().remove(article);
            }
        }
        this.articleRepository.deleteAllByIdInQuery(byNumber.stream().map(Article::getId).collect(Collectors.toList()));
    }

    public void findCommentsAndDelete(Account accountWithComments, String checked) throws NotFoundException {
        String[] split = checked.split(",");
        List<Long> collect = Arrays.stream(split).map(s -> {
            s = s.trim();
            return Long.valueOf(s);
        }).collect(Collectors.toList());

        List<Comments> byNumber = this.commentsRepository.findByNumber(collect);

        if (byNumber.isEmpty() || byNumber.size() != split.length) {
            throw new NotFoundException("Not Found Articles by Numbers");
        }

        for (Comments comments : byNumber) {
            if (accountWithComments.getComments().contains(comments)) {
                accountWithComments.getComments().remove(comments);
                comments.getArticle().getComments().remove(comments);
//                this.commentsRepository.delete(comments);
            }
        }
        this.commentsRepository.deleteAllByIdInQuery(byNumber.stream().map(Comments::getId).collect(Collectors.toList()));
//        this.commentsRepository.deleteInBatch(byNumber);
    }

    public void findNotificationAndDelete(Account accountWithNotification, String checked) throws NotFoundException {
        String[] split = checked.split(",");
        List<Long> collect = Arrays.stream(split).map(s -> {
            s = s.trim();
            return Long.valueOf(s);
        }).collect(Collectors.toList());

        List<Notification> byNumber = this.notificationRepository.findByNumber(collect);

        if (byNumber.isEmpty()) {
            throw new NotFoundException("Not Found Articles by Numbers");
        }

        for (Notification notification : byNumber) {
            if (accountWithNotification.getNotification().contains(notification)) {
                accountWithNotification.getNotification().remove(notification);
            }
        }
        this.notificationRepository.deleteAllByIdInQuery(byNumber.stream().map(Notification::getId).collect(Collectors.toList()));
//        this.notificationRepository.deleteInBatch(byNumber);
    }

    public Page<ArticleDTO> createPageableArticle(Long id, Pageable pageable, Account account) {
        Page<Article> pageableArticle = articleRepository.findByAccountId(id, pageable);
        return pageableArticle.map(article -> {
            ArticleDTO map = modelMapper.map(article, ArticleDTO.class);
            map.setAccountId(id);
            map.setUserName(account.getUsername());
            map.setUserEmail(account.getEmail());
            map.setAuthority(account.getAuthority() + "");
            map.setCommunityTitle(article.getCommunity().getTitle());
            return map;
        });
    }

    public Page<CommentsDTO> createPageableComments(Long id, Pageable pageable, Account account) {
        Page<Comments> pageableComments = commentsRepository.findByAccountId(id, pageable);
        return pageableComments.map(comments -> {
            CommentsDTO c = modelMapper.map(comments, CommentsDTO.class);
            if (comments.getArticle() != null) {
                c.setArticleId(comments.getArticle().getId());
                c.setArticleNumber(comments.getArticle().getNumber());
            }
            return c;
        });

    }

    public Page<NotificationDTO> createPageableNotification(Long id, Pageable pageable, Account account) {
        Page<Notification> pageableComments = notificationRepository.findByAccountId(id, pageable);
        return pageableComments.map(notification -> {
            NotificationDTO c = modelMapper.map(notification, NotificationDTO.class);
            if (notification.getArticle() != null) {
                c.setArticleId(notification.getArticle().getId());
                c.setArticleNumber(notification.getArticle().getNumber());
            }
            if (notification.getComments() != null) {
                c.setCommentsId(notification.getComments().getId());
                c.setCommentsNumber(notification.getComments().getNumber());
            }
            c.setAccountId(notification.getAccount().getId());
            c.setAccountUsername(notification.getAccount().getUsername());
            return c;
        });
    }

    public AccountForm wrappingStringToMap(String textPlain) {
        AccountForm accountForm = new AccountForm();
        String[] split = textPlain.split("\"");
        accountForm.setUsername(split[3]);
        accountForm.setEmail(split[7]);
        accountForm.setPassword(split[11]);
        accountForm.setCheckingPassword(split[15]);
        return accountForm;
    }
}
